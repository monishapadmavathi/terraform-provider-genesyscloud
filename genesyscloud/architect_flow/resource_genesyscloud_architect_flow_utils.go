package architect_flow

import (
	"fmt"
	"log"
	"path/filepath"
	"strconv"
	"strings"
	"sync"

	"github.com/hashicorp/terraform-plugin-sdk/v2/helper/schema"
)

var lock = sync.Mutex{}

func isForceUnlockEnabled(d *schema.ResourceData) bool {
	forceUnlock := d.Get("force_unlock").(bool)
	log.Printf("ForceUnlock: %v, id %v", forceUnlock, d.Id())

	if forceUnlock && d.Id() != "" {
		return true
	}
	return false
}

func GenerateFlowResource(resourceID, srcFile, fileContent string, forceUnlock bool, substitutions ...string) string {
	lock.Lock()
	fullyQualifiedPath, _ := filepath.Abs(srcFile)

	if fileContent != "" {
		updateFile(srcFile, fileContent)
	}

	flowResourceStr := fmt.Sprintf(`resource "genesyscloud_flow" "%s" {
        filepath = %s
		file_content_hash =  filesha256(%s)
		force_unlock = %v
		%s
	}
	`, resourceID, strconv.Quote(srcFile), strconv.Quote(fullyQualifiedPath), forceUnlock, strings.Join(substitutions, "\n"))
	defer lock.Unlock()
	return flowResourceStr
}

// setFileContentHashToNil This operation is required after a flow update fails because we want Terraform to detect changes
// in the file content hash and re-attempt an update, should the user re-run terraform apply without making changes to the file contents
func setFileContentHashToNil(d *schema.ResourceData) {
	_ = d.Set("file_content_hash", nil)
}

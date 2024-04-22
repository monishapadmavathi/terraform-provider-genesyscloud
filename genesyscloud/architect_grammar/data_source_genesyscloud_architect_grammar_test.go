package architect_grammar

import (
	"fmt"
	"log"
	"terraform-provider-genesyscloud/genesyscloud/provider"
	"terraform-provider-genesyscloud/genesyscloud/util"
	"testing"

	"github.com/google/uuid"
	"github.com/hashicorp/terraform-plugin-sdk/v2/helper/resource"

	"github.com/mypurecloud/platform-client-sdk-go/v125/platformclientv2"
)

var (
	sdkConfig *platformclientv2.Configuration
)

func TestAccDataSourceArchitectGrammar(t *testing.T) {
	var (
		grammarResource = "grammar-resource"
		grammarData     = "grammar-data"
		name            = "GrammarArchitect" + uuid.NewString()
		description     = "Sample description"
	)

	cleanupArchitectGrammar("GrammarArchitect")
	resource.Test(t, resource.TestCase{
		PreCheck:          func() { util.TestAccPreCheck(t) },
		ProviderFactories: provider.GetProviderFactories(providerResources, providerDataSources),
		Steps: []resource.TestStep{
			{
				Config: GenerateGrammarResource(
					grammarResource,
					name,
					description,
				) + generateGrammarDataSource(
					grammarData,
					name,
					"genesyscloud_architect_grammar."+grammarResource,
				),
				Check: resource.ComposeTestCheckFunc(
					resource.TestCheckResourceAttrPair("data.genesyscloud_architect_grammar."+grammarData, "id", "genesyscloud_architect_grammar."+grammarResource, "id"),
				),
			},
		},
	})
}

func generateGrammarDataSource(
	resourceID string,
	name string,
	dependsOnResource string) string {
	return fmt.Sprintf(`data "genesyscloud_architect_grammar" "%s" {
		name = "%s"
		depends_on=[%s]
	}
	`, resourceID, name, dependsOnResource)
}

func cleanupArchitectGrammar(idPrefix string) {
	architectApi := platformclientv2.NewArchitectApi(sdkConfig)

	for pageNum := 1; ; pageNum++ {
		const pageSize = 100
		architectGrammars, _, getErr := architectApi.GetArchitectGrammars(pageNum, pageSize, "", "", nil, idPrefix, "", "", false)
		if getErr != nil {
			log.Printf("failed to get architect grammar %s", getErr)
			return
		}

		if architectGrammars.Entities == nil || len(*architectGrammars.Entities) == 0 {
			break
		}

		for _, grammar := range *architectGrammars.Entities {
			if grammar.Name != nil {
				_, _, delErr := architectApi.DeleteArchitectGrammar(*grammar.Id)
				if delErr != nil {
					log.Printf("failed to delete architect grammar %s", delErr)
					return
				}
				log.Printf("Deleted architect grammar %s (%s)", *grammar.Id, *grammar.Name)
			}
		}
	}
}

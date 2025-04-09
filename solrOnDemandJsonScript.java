import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import com.day.cq.dam.api.AssetManager
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.json.*

String solrServerUrl = "https://devsearch.bdbiosciences.com/solr/bdbio-us"
int batch = 1
def solrList = [];
final APPLICATION_PDF = "application/pdf"


JsonSlurper slurper = new JsonSlurper()

// Create a SolrClient instance
HttpSolrClient solrClient = new HttpSolrClient.Builder(solrServerUrl).build()
def solrService = getService("com.bdb.aem.core.services.solr.IcmSolrSearchQueriesService")
def helperService = getService("com.bdb.aem.core.services.WorkflowHelperService")

def assetMan = resourceResolver.adaptTo(AssetManager.class);
// Get the current session
def session = resourceResolver.adaptTo(Session)

int j = 100

try {
    for (int i = 0; i < 13029; i++) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        solrQuery.addFilterQuery("tdsDocumentType_t:\""+"ON_DEMAND_TDS\"")
        solrQuery.addFilterQuery("docType_t:product")
        solrQuery.setFields("materialNumber_t","damGlobalPath_t","tdsDocumentType_t")
        solrQuery.setStart(i)
        solrQuery.setRows(100)
        QueryResponse response = solrClient.query(solrQuery)
        println("solrQuery response :" + response.toString())
        SolrDocumentList sitesSolrDocs = response.getResults();
        solrList = solrService.getResultsJson(sitesSolrDocs)
        // println("response:"+solrList)
        def JsonObject = [:]
        JsonObject.key = solrList
        def quotes ='"'
        JsonObject = JsonObject.toString().replaceAll("key",quotes+"docs"+quotes).replaceAll("=",":").replaceFirst("\\[","{")
        JsonObject = JsonObject.toString().reverse().replaceFirst("\\]","}").reverse()
        // String jsonResponse = JsonObject.toString()
        println("solrDoc response :" +JsonObject.toString())
        String JsonResponse = JsonOutput.prettyPrint(JsonObject.toString())
        //  println("resultString :" +JsonResponse.toString())
        // define dam folder path
        String damFolderPath = "/content/dam/bdb/my-folder"
        // define the desired file name
        String fileName = i+"-"+j+"_"+"Batch-"+batch
        println("fileName :" +fileName)
        
        def assetInputStream =  new ByteArrayInputStream(JsonResponse.getBytes())
        //create dam asset with jcr content
        def asset = helperService.createAsset(assetMan, assetInputStream, damFolderPath + "/" +fileName, APPLICATION_PDF, resourceResolver, session)
        // def asset = damSession.createAsset(damFolderPath + "/" +fileName, new ByteArrayInputStream(JsonResponse.getBytes()),"application/json",true)
        i = i + 100
        j = j + 101
        batch ++
    }
    
    // Save the changes to the session
    session.save()
    
    } catch (Exception e) {
    println("error" +e.getMessage())
    } finally {
    solrClient.close()
    }
    

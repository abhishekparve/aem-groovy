// Script to generate and store TDS for products having document type as ON_DEMAND_TDS 
import groovy.json.JsonSlurper
import org.json.*
import java.nio.charset.StandardCharsets
import java.nio.charset.Charset
import javax.jcr.Node
import javax.jcr.Session
 
def filePath = "/content/dam/bdb/on-demand-tds-products/3030-3130_products.json"
 
// Getting the res 
Resource res = resourceResolver.getResource(filePath)
Asset asset = res.adaptTo(Asset.class)
 
// Getting the inputstream
InputStream data = asset.getOriginal().getStream()
 
// Defining String builder 
def sb = ''<<'', line
 
BufferedReader br = new BufferedReader(new InputStreamReader(
data, StandardCharsets.UTF_8));
 
while ((line = br.readLine()) != null) {
    sb.append(line);
}
 
JsonSlurper slurper = new JsonSlurper()
 
def userConfig = slurper.parseText(sb.toString())
 
def path = userConfig.docs.damGlobalPath_t
 
def skuName = userConfig.docs.materialNumber_t
 
// Document Generation Script 
def tdsGenerationService = getService("com.bdb.aem.core.services.OnDemandTdsGenerationService")
def helperService = getService("com.bdb.aem.core.services.WorkflowHelperService")
def catalogService = getService("com.bdb.aem.core.services.CatalogStructureUpdateService")
 
final MATERIAL_NUMBER = "materialNumber", APPLICATION_PDF = "application/pdf", PDF = "/pdf/", DOT_PDF = ".pdf"
final METADATAPATH = "/jcr:content/metadata", DOC_TYPE = "docType", DATA_TDS = "Technical data sheet (TDS)"
final TDS_DOCUMENT_TYPE = "tdsDocumentType", ON_DEMAND_TDS = "ON_DEMAND_TDS", PDFX_DOC_TYPE = "pdfx:docType"
 
// Get the current session
def session = resourceResolver.adaptTo(Session)
int loopCounter = 0
 
for(int i = 0; i < skuName.size(); i++){
    loopCounter++
    // Get the resource 
    def variantRes = catalogService.getProductFromLookUp(resourceResolver, skuName[i], MATERIAL_NUMBER)
    def damPath = path[i] + PDF + skuName[i] + DOT_PDF
    def assetMan = resourceResolver.adaptTo(AssetManager.class);
    if (null == resourceResolver.getResource(damPath)) {
        def pdfStream = tdsGenerationService.getPdfStream(variantRes, resourceResolver, skuName[i])
        def assetInputStream = new ByteArrayInputStream(pdfStream.toByteArray())
        def pdfAsset = helperService.createAsset(assetMan, assetInputStream, damPath, APPLICATION_PDF, resourceResolver, session)
        def pdfMetadataResource = resourceResolver.getResource(damPath + METADATAPATH)
        println 'SkuName : ' + skuName[i]
        println 'DamPath : ' + damPath + "\n"
        if (null != pdfMetadataResource) {
            Node currentNode = pdfMetadataResource.adaptTo(Node.class)
            // set tdsDocType and docType for indexing and PDP pages
            currentNode.setProperty(DOC_TYPE, DATA_TDS)
            currentNode.setProperty(TDS_DOCUMENT_TYPE, ON_DEMAND_TDS)
            // set docType metadata for scientific resource page
            currentNode.setProperty(PDFX_DOC_TYPE, DATA_TDS)
        }
        // Save the changes to the session
        session.save()
    } else {
        println 'On demand TDS exist for : ' + skuName[i]
    }
    
}
 println 'loop Counter : ' + loopCounter

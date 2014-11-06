package jp.aegif.nemaki.api.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jp.aegif.nemaki.query.solr.SolrUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Node;

@Path("/search-engine")
public class SolrResource extends ResourceBase {
	
	private SolrUtil solrUtil;

	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	public String initialize() {
		boolean status = true;
		JSONObject result = new JSONObject();
		JSONArray errMsg = new JSONArray();
		
		//Call Solr
		HttpClient httpClient = HttpClientBuilder.create().build();
		String solrUrl = solrUtil.getSolrUrl();
		String url = solrUrl + "admin/cores?core=nemaki&action=init";
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			int responseStatus = response.getStatusLine().getStatusCode();
			if(HttpStatus.SC_OK != responseStatus){
				throw new Exception("Solr server connection failed");
			}
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if(checkSuccess(body)){
				status = true;
			}else{
				status = false;
				//TODO error message
			}
		} catch (Exception e) {
			status = false;
			//TODO error message
			e.printStackTrace();
		}

		// Output
		result = makeResult(status, result, errMsg);
		return result.toString();
	}
	
	@GET
	@Path("/reindex")
	@Produces(MediaType.APPLICATION_JSON)
	public String reindex() {
		boolean status = true;
		JSONObject result = new JSONObject();
		JSONArray errMsg = new JSONArray();
		
		//Call Solr
		HttpClient httpClient = HttpClientBuilder.create().build();
		String solrUrl = solrUtil.getSolrUrl();
		String url = solrUrl + "admin/cores?core=nemaki&action=index&tracking=FULL";
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			int responseStatus = response.getStatusLine().getStatusCode();
			if(HttpStatus.SC_OK != responseStatus){
				throw new Exception("Solr server connection failed");
			}
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if(checkSuccess(body)){
				status = true;
			}else{
				status = false;
				//TODO error message
			}
		} catch (Exception e) {
			status = false;
			//TODO error message
			e.printStackTrace();
		}

		// Output
		result = makeResult(status, result, errMsg);
		return result.toString();
	}
	
	
	private boolean checkSuccess(String xml) throws Exception{
		//sanitize
		xml = xml.replace("\n", "");
		
		//parse
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		//traverse
		InputStream bais = new ByteArrayInputStream(xml.getBytes("utf-8")); 
		Node root = db.parse(bais);
		Node response = root.getFirstChild();
		Node lst = response.getFirstChild();
		Node status = lst.getFirstChild();
		
		//check
		return "0".equals(status.getTextContent());
	}

	public void setSolrUtil(SolrUtil solrUtil) {
		this.solrUtil = solrUtil;
	}
	
}
package cheng.Bioinfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.*;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.nicta.uima.type.Mention;
import org.nicta.uima.type.Relation;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BioinferReader extends JCasCollectionReader_ImplBase{
	//private static Logger logger = LoggerFactory.getLogger(StandfordReader.class);

	public static final String PARAM_INPUTDIR = "inputFile";
	@ConfigurationParameter(name = PARAM_INPUTDIR, mandatory = true)
	private String inputFile;	
	
	private BufferedReader in;
	private LinkedList<List<String>> lines;
	private List<File> files;
	private List<File> usefulfiles;
	private int index;
	HashSet<Integer> map;
	private HashMap<String, String> nerType;
	int offset=0;
	
	String tag_start = ">";
	String tag_end = "</";
	String wordtag_start = "<w id=";
	
	
	//this function mainly load bioinfer data
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		File dir = new File(inputFile);
		System.out.println(dir + dir.getAbsolutePath());
		if (dir.isDirectory()){
			System.out.println("in");
			files = (List<File>) FileUtils.listFiles(dir,null, false);
			int count = 0;
			
			//pick out xml files and save in usefulfiles
			for(File tempfile : files){
				
				//System.out.println(count++);
				String tempfilename = tempfile.getName();
				
				String patternString = "\\d{6}.xml";
				Pattern pattern = Pattern.compile(patternString);
				Matcher m = pattern.matcher(tempfilename);
				
				//System.out.println(tempfilename);
				if(m.find()){
					System.out.println(tempfilename + "ok");
					usefulfiles.add(tempfile);
				}
				else{
					continue;
				}
			}
			
			
		}
		else{
			System.out.println("bad");
		}
		

	}
	
	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		// TODO Auto-generated method stub
		for(File xmlfile : usefulfiles){
			DocumentBuilder domBuilder;
			try {
				domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				
				InputStream input = new FileInputStream(xmlfile);
				Document doc;
				doc = domBuilder.parse(input);
				//Element root = doc.getDocumentElement();
				
				NodeList words = doc.getElementsByTagName("w");
				HashMap<String,Integer> words_offset = new HashMap<>();
				//get the whole sentence
				String sentence = "";
				int offset = 0;
				for(int i = 0; i < words.getLength(); i++){
					String temp = words.item(i).getTextContent();
					if(i < words.getLength() - 1){
						words_offset.put(temp,offset);
						sentence +=  temp + " ";
						offset = offset + temp.length()+1;
					}
					else{
						words_offset.put(temp,offset);
						sentence += words.item(i).getTextContent();
					}
				}
				//System.out.println(xmlfile.getName()+ words.getLength()+sentence);
				
				//get the entities list
				HashMap<String,String> entityMap = new HashMap<>();
				NodeList entities = doc.getElementsByTagName("ne");
				for(int i = 0; i < entities.getLength(); i++){
					String ent_id = entities.item(i).getAttributes().getNamedItem("id").getNodeValue();
					ent_id = ent_id.replaceAll("\r|\n", "");
					String ent_content = entities.item(i).getTextContent();
					ent_content = ent_content.replaceAll("\r|\n", "");
					ent_content = ent_content.trim();
					entityMap.put(ent_id, ent_content);
					if(i == 0){
						System.out.println(xmlfile.getName()+ent_id+ent_content);
					}
				}
				//System.out.println(entityMap.toString());
				
				NodeList relation = doc.getElementsByTagName("rel");
				if(relation.getLength() != 0){
					for(int i = 0; i < relation.getLength();i++){
						//String 
						String rel_ent1_eid = relation.item(i).getAttributes().getNamedItem("e1").getNodeValue();
						String ent1_content = entityMap.get(rel_ent1_eid);
						int ent1_start = words_offset.get(rel_ent1_eid);
						int ent1_end = ent1_start + ent1_content.length();
						//Mention entity_1 = new Mention(jcas, ent1_start, ent1_end);
						//entity_1.addToIndexes();
					}
				}
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean hasNext() {
		return !usefulfiles.isEmpty();
	}
	
	@Override
	public void destroy() {
		try {
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		super.destroy();
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

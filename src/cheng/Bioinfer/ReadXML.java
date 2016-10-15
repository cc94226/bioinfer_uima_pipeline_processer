package cheng.Bioinfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//cheng 15/10/2016

public class ReadXML {

	private String inputDir = "reBioInfer";
	private List<File> files;
	private ArrayList<File> usefulfiles;
	
	public String tag_start = "\">";
	static String tag_end = "</";
	static String wordtag_start = "<w id=";
	
	class Word_inf{
		//public String id;
		public String content;
		public int start;
		public int end;
	}
	
	class Ent_inf{
		public String content;
		public String type;
		public int start;
		public int end;
	}
	
	public void process(){
		File dir = new File(inputDir);
		this.usefulfiles = new ArrayList<File>();
		System.out.println(dir + dir.getAbsolutePath());
		if (dir.isDirectory()){
			System.out.println("in");
			files = (List<File>) FileUtils.listFiles(dir,null, false);
			int count = 0;
			for(File tempfile : files){
				
				//System.out.println(count++);
				String tempfilename = tempfile.getName();
				
				String patternString = "\\d{6}.xml";
				Pattern pattern = Pattern.compile(patternString);
				Matcher m = pattern.matcher(tempfilename);
				
				//System.out.println(tempfilename);
				if(m.find()){
					//System.out.println(tempfilename + "ok");
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
		
		for(File xmlfile : usefulfiles){
			
			try {
				DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputStream input = new FileInputStream(xmlfile);
				Document doc = domBuilder.parse(input);
				//Element root = doc.getDocumentElement();
				
				NodeList words = doc.getElementsByTagName("w");
				//HashMap<String,Integer> words_offset = new HashMap<>();
				//LinkedList<Word_inf> wors_list = new LinkedList<Word_inf>();
				HashMap<String,Word_inf> words_list = new HashMap<>();
				//get the whole sentence
				String sentence = "";
				Integer offset = 0;
				for(int i = 0; i < words.getLength(); i++){
					String temp = words.item(i).getTextContent();
					if(i < words.getLength() - 1){
						//words_offset.put(temp,offset);						
						sentence +=  temp + " ";
						Word_inf word_inf = new Word_inf();
						//word_inf.id = words.item(i).getAttributes().getNamedItem("id").getNodeValue();
						word_inf.content = temp;
						word_inf.start = offset;
						word_inf.end = offset + temp.length();
						words_list.put(words.item(i).getAttributes().getNamedItem("id").getNodeValue(), word_inf);
						offset = offset + temp.length()+1;
						
					}
					else{
						//words_offset.put(temp,offset);
						sentence += words.item(i).getTextContent();
						Word_inf word_inf = new Word_inf();
						//word_inf.id = words.item(i).getAttributes().getNamedItem("id").getNodeValue();
						word_inf.content = temp;
						word_inf.start = offset;
						word_inf.end = offset + temp.length();
						words_list.put(words.item(i).getAttributes().getNamedItem("id").getNodeValue(), word_inf);
					}
					
				}
				//System.out.println(xmlfile.getName()+ words.getLength()+sentence);
				
				//get the entities list
//				ent_id,ent_content
				HashMap<String,Ent_inf> entityMap = new HashMap<>();
				//		ent_id,ent_type
				//HashMap<String,String> ent_type_map = new HashMap<>();
				NodeList entities = doc.getElementsByTagName("ne");
				for(int i = 0; i < entities.getLength(); i++){
					String ent_id = entities.item(i).getAttributes().getNamedItem("id").getNodeValue();
					ent_id = ent_id.replaceAll("\r|\n", "");
					String ent_content = entities.item(i).getTextContent();
					ent_content = ent_content.replaceAll("\r|\n", "");
					ent_content = ent_content.trim();
					String ent_type = entities.item(i).getAttributes().getNamedItem("t").getNodeValue();
					
					Ent_inf ent = new Ent_inf();
					ent.content = ent_content;
					Word_inf word_start = words_list.get(entities.item(i).getAttributes().getNamedItem("fr").getNodeValue());
					ent.start = word_start.start;
					Word_inf word_end = words_list.get(entities.item(i).getAttributes().getNamedItem("to").getNodeValue());
					ent.start = word_start.end;
					ent.type = ent_type;
					
					entityMap.put(ent_id, ent);
					
					
					//ent_type_map.put(ent_id, ent_type);
					
					if(i == 0){
						//System.out.println(xmlfile.getName()+ent_id+ent_content + ent_type);
					}
				}	
				//System.out.println(entityMap.toString());
				
				NodeList relation_list = doc.getElementsByTagName("rel");
				if(relation_list.getLength() != 0){
					System.out.println(xmlfile.getName());
					for(int i = 0; i < relation_list.getLength();i++){
						//String 
						String ent1_eid = relation_list.item(i).getAttributes().getNamedItem("e1").getNodeValue();
						Ent_inf ent1 = entityMap.get(ent1_eid);
						//int ent1_start = words_offset.get(ent1_eid);
						//int ent1_end = ent1_start + ent1_content.length();
						//Mention entity_1 = new Mention(jcas, ent1_start, ent1_end);
						//entity_1.addToIndexes();
						//entity_1.setValue(ent_type_map.get(ent1_eid));
						
						String ent2_eid = relation_list.item(i).getAttributes().getNamedItem("e2").getNodeValue();
						if(ent2_eid.contains(",")){
							String[] ent2_sublist = ent2_eid.split(",");
							for(String subent_id : ent2_sublist){
								Ent_inf subent = entityMap.get(subent_id);
								String rel = relation_list.item(i).getAttributes().getNamedItem("t").getNodeValue();
								System.out.println(ent1.content + " "+ rel + " "+ subent.content);
								
							}
						}
						else{
							Ent_inf ent2 = entityMap.get(ent2_eid);
							//int ent2_start = words_offset.get(ent2_eid);
							//int ent2_end = ent2_start + ent2_content.length();
							//Mention entity_2 = new Mention(jcas, ent2_start, ent2_end);
							//entity_2.addToIndexes();
							//entity_2.setValue(ent_type_map.get(ent2_eid));
							
							String rel = relation_list.item(i).getAttributes().getNamedItem("t").getNodeValue();
							
							System.out.println(ent1.content + " "+ rel + " "+ ent2.content);
						}
					}
				}
				
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			/*String line = "";
			String sentence = "";
			
			InputStream stream;
			try {
				stream = new FileInputStream(xmlfile);
			
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(stream));
				int c = 0;
				while ((line = reader.readLine()) != null) {
					c++;
					if(line.contains(wordtag_start)){
						int start = line.indexOf(tag_start);
						int end = line.indexOf(tag_end);
						System.out.println(start + line.substring(start, end));
						sentence += line.substring(start, end) + " ";
					}
				}
				System.out.println(c + sentence);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReadXML rxml = new ReadXML();
		rxml.process();
	}

}

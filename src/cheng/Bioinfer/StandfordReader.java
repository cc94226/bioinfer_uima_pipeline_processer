package org.nicta.uima.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.nicta.uima.type.Mention;
import org.nicta.uima.type.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class StandfordReader extends JCasCollectionReader_ImplBase {

	private static Logger logger = LoggerFactory.getLogger(StandfordReader.class);

	public static final String PARAM_INPUTDIR = "inputFile";
	
	@ConfigurationParameter(name = PARAM_INPUTDIR, mandatory = true)
	private String inputFile;	
	
	private BufferedReader in;
	private LinkedList<List<String>> lines;
	private List<File> files;
	private int index;
	HashSet<Integer> map;
	private HashMap<String, String> nerType;
	int offset=0;
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		/*HashSet<Integer> set = new HashSet<>();
		try {
			Scanner sc = new Scanner(new File("/Users/zhuangli/Documents/scalaworkspace/DL-IE/log1"));
			while (sc.hasNextLine()) {
				set.add(Integer.parseInt(sc.nextLine()));
				
			}
			sc.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		map=new HashSet<>();
		File dir = new File(inputFile);
		lines = new LinkedList<List<String>>();
		nerType=new HashMap<>();
		initialNerMap(nerType);
		if (dir.isDirectory()) {
			files = (List<File>) FileUtils.listFiles(dir,null, false);
			String line = "";
			int count=0;
			for (File file : files) {
				if (file.isHidden()) {
					continue;
				}
				try {
					int mark=0;
					InputStream stream = new FileInputStream(file);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(stream));
					while ((line = reader.readLine()) != null) {
						if(mark==0)
						{
							mark++;
							continue;
						}
						//Split the chart into each cell
						String lineArray[]=line.split(",");
						/*if(index<4108){
							index++;
							continue;
						}*/
						
						//CHeng: split sentences in sentence cell
						if(lineArray[4].split(" ").length<100){
						lines.add(Arrays.asList(lineArray));
						offset++;
						}else{
							map.add(offset);
							offset++;
							continue;
						}
						mark++;
						count++;
						
					}
					reader.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			logger.info("Read " + (count) + " lines");
		}else{
			throw new ResourceInitializationException(new IllegalArgumentException(dir + " is not a directory. Please make sure " + PARAM_INPUTDIR + " is set propertly."));
		}
	}

	private void initialNerMap(HashMap<String, String> nerType2) {
		// TODO Auto-generated method stub
		nerType2.put("per", "PERSON");
		nerType2.put("spouse", "PERSON");
		nerType2.put("siblings", "PERSON");
		nerType2.put("date_of_birth", "TIME");
		nerType2.put("country_of_death", "LOCATION");
		nerType2.put("origin", "LOCATION");
		nerType2.put("title", "TITLE");
		nerType2.put("cities_of_residence", "LOCATION");
		nerType2.put("stateorprovinces_of_residence", "LOCATION");
		nerType2.put("stateorprovince_of_death", "LOCATION");
		nerType2.put("city_of_headquarters", "LOCATION");
		nerType2.put("country_of_headquarters", "LOCATION");
		nerType2.put("city_of_death", "LOCATION");
		nerType2.put("stateorprovince_of_headquarters", "LOCATION");
		nerType2.put("countries_of_residence", "LOCATION");
		nerType2.put("date_of_death", "TIME");
		nerType2.put("children", "PERSON");
		nerType2.put("political/religious_affiliation", "ORGANIZATION");
		nerType2.put("members", "PERSON");
		nerType2.put("country_of_birth", "LOCATION");
		nerType2.put("member_of", "ORGANIZATION");
		nerType2.put("shareholders", "PERSON");
		nerType2.put("number_of_employees/members", "NUMBER");
		nerType2.put("religion", "RELIGION");
		nerType2.put("schools_attended", "ORGANIZATION");
		nerType2.put("alternate_names", "NAMES");
		nerType2.put("charges", "MONEY");
		nerType2.put("employee_of", "ORGANIZATION");
		nerType2.put("other_family", "PERSON");
		nerType2.put("subsidiaries", "ORGANIZATION");
		nerType2.put("stateorprovince_of_birth", "LOCATION");
		nerType2.put("top_members/employees", "PERSON");
		nerType2.put("city_of_birth", "LOCATION");
		nerType2.put("age", "TIME");
		nerType2.put("parents", "PERSON");
		
		
		
		
	}

	/**
	 * @see org.apache.uima.collection.CollectionReader#hasNext()
	 */
	public boolean hasNext() {
		return !lines.isEmpty();
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		String lineArray[]=(String[])lines.poll().toArray();
		int entity_1s=Integer.parseInt(lineArray[6].replace("\"", ""));
		int entity_1e=Integer.parseInt(lineArray[7].replace("\"", ""));
		int entity_2s=Integer.parseInt(lineArray[9].replace("\"", ""));
		int entity_2e=Integer.parseInt(lineArray[10].replace("\"", ""));
		Mention entity_1=new Mention(jcas, entity_1s, entity_1e);
		Mention entity_2=new Mention(jcas, entity_2s, entity_2e);
		entity_1.addToIndexes();
		entity_2.addToIndexes();
		int begin=0;
		int end=0;
		Relation relation=new Relation(jcas);
		//check the order of the mentions
		if (entity_1s<entity_2s) {
			begin=entity_1s;
			end=entity_2e;
			relation.setBegin(begin);
			relation.setEnd(end);
			relation.setEntity_1(entity_1);
			relation.setEntity_2(entity_2);
		}else{
			begin=entity_2s;
			end=entity_1e;
			relation.setBegin(begin);
			relation.setEnd(end);
			relation.setEntity_1(entity_2);
			relation.setEntity_2(entity_1);
		}
		
		String relationArray[]=lineArray[1].replace("\"", "").split(":");
		if (relationArray.length>1) {
			entity_1.setValue(relationArray[0]);
			entity_2.setValue(nerType.get(relationArray[1]));
		}
		relation.setRelation(lineArray[1].replace("\"", ""));
		relation.addToIndexes();
		String sentenceString=lineArray[4].replace("\"", "");
		Sentence sentence=new Sentence(jcas, 0, sentenceString.length());
		
		sentence.addToIndexes(jcas);
		DocumentMetaData metaData = new DocumentMetaData(jcas, 0, sentenceString.length());
		jcas.setDocumentText(sentenceString);
		System.out.println(index);
		System.out.println(sentence.getCoveredText());
		while(map.contains(index)){
			index++;
		}
		//System.out.println(sentenceString);
		metaData.setDocumentId(""+index);
		metaData.addToIndexes(jcas);
		index++;
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

package cheng.Bioinfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

public class BioinferReader extends JCasCollectionReader_ImplBase{
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
	
	//this function mainly load bioinfer data
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		File input_file = new File(inputFile);
		InputStream in_stream = new FileInputStream(input_file);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(in_stream));
		String temp_line = "";
		while ((temp_line = reader.readLine()) != null) {
			
		}
	}
	
	@Override
	public void getNext(JCas arg0) throws IOException, CollectionException {
		// TODO Auto-generated method stub
		
	}
	
	public boolean hasNext() {
		return !lines.isEmpty();
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

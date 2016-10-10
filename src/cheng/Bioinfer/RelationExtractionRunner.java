package org.nicta.uima.runners;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.fit.pipeline.SimplePipeline;

import org.apache.uima.resource.ResourceInitializationException;
import org.nicta.uima.ae.preprocessing.SelfAdjustedStanfordSegmenter;
import org.nicta.uima.reader.StandfordReader;
import org.nicta.uima.reader.utils.RelationCorporaAnalysis;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;

public class RelationExtractionRunner extends RunnerComponents {
	static final String inputFiles = "/home/data61/Stanford IE data/mimlre-2012-11-27";
	static final String outputFiles = "/home/data61/result";

	public RelationExtractionRunner(String inputFiles, String outputFiles) {
		super(inputFiles, outputFiles);
		// TODO Auto-generated constructor stub
	}

	@Override
	public CollectionReaderDescription createReader()
			throws ResourceInitializationException {
		CollectionReaderDescription cr = createReaderDescription(
				StandfordReader.class, StandfordReader.PARAM_INPUTDIR,
				inputFiles);

		return cr;
	}

	public AnalysisEngineDescription createRelationReportDescription(
			String corpraName, String dir) {
		AnalysisEngineDescription ae = null;
		try {
			ae = createEngineDescription(RelationCorporaAnalysis.class,
					RelationCorporaAnalysis.PARAM_RELATION_CORPORA_NAME,
					corpraName, RelationCorporaAnalysis.PARAM_TARGET_LOCATION,
					dir);
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ae;
	}

	// val seg: AnalysisEngineDescription =
	// createEngineDescription(classOf[SelfAdjustedStanfordSegmenter]);
	// val parser: AnalysisEngineDescription =
	// createEngineDescription(classOf[StanfordParser],
	// StanfordParser.PARAM_LANGUAGE, "en", StanfordParser.PARAM_VARIANT,
	// "wsj-pcfg", StanfordParser.PARAM_MODE, "BASIC");
	// val ner: AnalysisEngineDescription =
	// createEngineDescription(classOf[StanfordNamedEntityRecognizer],
	// StanfordNamedEntityRecognizer.PARAM_LANGUAGE, "en",
	// StanfordNamedEntityRecognizer.PARAM_VARIANT, "conll.4class.distsim.crf");
	// val chunk: AnalysisEngineDescription =
	// createEngineDescription(classOf[OpenNlpChunker],
	// OpenNlpChunker.PARAM_LANGUAGE, "en", OpenNlpChunker.PARAM_VARIANT,
	// "default");
	// val instCreatorConfig: InstanceCreatorConfig = new
	// InstanceCreatorConfig(classOf[SnippetInstSeqCreator])
	// val head: AnalysisEngineDescription =
	// createEngineDescription(classOf[HeadersInTheMentions]);

	public AnalysisEngineDescription createSegmenterDescription() {
		AnalysisEngineDescription ae = null;
		try {
			ae = createEngineDescription(SelfAdjustedStanfordSegmenter.class);
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ae;
	}

	public AnalysisEngineDescription createStanfordParserDescription() {
		AnalysisEngineDescription ae = null;
		try {
			ae = createEngineDescription(StanfordParser.class,
					StanfordParser.PARAM_LANGUAGE, "en",
					StanfordParser.PARAM_VARIANT, "wsj-pcfg",
					StanfordParser.PARAM_MODE, "BASIC");
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ae;
	}

	public AnalysisEngineDescription createChunkerDescription() {
		AnalysisEngineDescription ae = null;
		try {
			ae = createEngineDescription(OpenNlpChunker.class,
					OpenNlpChunker.PARAM_LANGUAGE, "en",
					OpenNlpChunker.PARAM_VARIANT, "default");
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ae;
	}

	public AnalysisEngineDescription createNerDescription() {
		AnalysisEngineDescription ae = null;
		try {
			ae = createEngineDescription(StanfordNamedEntityRecognizer.class,
					StanfordNamedEntityRecognizer.PARAM_LANGUAGE, "en",
					StanfordNamedEntityRecognizer.PARAM_VARIANT,
					"conll.4class.distsim.crf");
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ae;
	}

	public static void main(String[] args) throws IOException, UIMAException,
			SAXException, CpeDescriptorException {
		// if (args.length > 1) {
		// String inputDir = args[0];
		// String outputDir = args[1];
		RelationExtractionRunner relationExtractionRunner = new RelationExtractionRunner(
				inputFiles, outputFiles);
		SimplePipeline.runPipeline(relationExtractionRunner.createReader(),
				relationExtractionRunner.createSegmenterDescription(),
				relationExtractionRunner.createStanfordParserDescription(),
				relationExtractionRunner.createNerDescription(),
				relationExtractionRunner.createChunkerDescription(),
				relationExtractionRunner.createRelationReportDescription(
						"stanfordData", outputFiles), relationExtractionRunner
						.writeToUnzip());
		// } else {
		// throw new IllegalArgumentException(
		// "please enter input and output file/dir path!");
		// }
	}
}

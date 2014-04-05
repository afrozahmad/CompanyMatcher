package com.intuit.ems.hre;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	Directory directory;
	Analyzer analyzer;
	IndexSearcher searcher=null;
	public Indexer() throws IOException{
		analyzer = new Analyzer() {
		@Override
		public TokenStream tokenStream(String fieldName, Reader reader) {
			TokenStream input = new NGramTokenizer(reader, 2,6);
			//TokenStream input = new WhitespaceTokenizer(Version.LUCENE_36, reader); 

			input = new LowerCaseFilter(Version.LUCENE_36, input); 

//			input = new ShingleFilter(input,2,4);
//			((ShingleFilter)input).setOutputUnigrams(false);
//			//TokenStream input = new NGramTokenizer(reader, 2,6);
			input = new LowerCaseFilter(Version.LUCENE_35, input);
			
			return input;
		}
		};
		directory = FSDirectory.open(new File("/Users/afroza786/Documents/company_index_span"));
	}
	
	public void init(Path path ) throws IOException{
		//directory = new RAMDirectory();
		 //write on disk		
			
		
		
		   Map<String, Analyzer> analyzerPerField = new HashMap<>();
		   analyzerPerField.put("fein", new KeywordAnalyzer());

		   PerFieldAnalyzerWrapper aWrapper =
		      new PerFieldAnalyzerWrapper(analyzer, analyzerPerField);
		
		
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, aWrapper);
			IndexWriter writer = new IndexWriter(directory, config);

			Scanner scanner =  new Scanner(path);
			scanner.nextLine(); //skip header;
		
			int l = 0;
			while (scanner.hasNextLine()){
				String row = scanner.nextLine();
				String [] data = row.split("\t");
				Document doc = new Document();
				doc.add(new Field("name", data[3].replaceAll("[\\.,']", "" ).trim(), Field.Store.YES, Field.Index.ANALYZED));
				doc.add(new Field("state", data[12].trim(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				
				doc.add(new Field("city", data[10].trim(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("address", data[9].trim(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("fein", data[2].trim(), Field.Store.YES, Field.Index.ANALYZED));
				writer.addDocument(doc);
				l++;
			}
			System.out.println("Indexed " + l + "docs");
			
			scanner.close();
			writer.commit();
			writer.close();
			
			searcher = new IndexSearcher(IndexReader.open(directory));
			
			
			
			
	}

	public TreeSet<Company> suggest (String input, int num) throws CorruptIndexException, IOException{
		TreeSet<Company> companies=new TreeSet<>();
		if (searcher == null){
			searcher = new IndexSearcher(IndexReader.open(directory));
		}
		final BooleanQuery mainBooleanQuery = new BooleanQuery(true);
		TokenStream ts = analyzer.tokenStream("name", new StringReader(input.replaceAll("[\\.,']", "" )));
		//List<SpanQuery> phrase = new ArrayList<SpanQuery>();
		while (ts.incrementToken()) {
			String term = ((CharTermAttribute) 			ts.getAttribute(CharTermAttribute.class)).toString();
			mainBooleanQuery .add(new TermQuery(new Term("name", term)), BooleanClause.Occur.SHOULD);
			//phrase.add(new SpanTermQuery(new Term("name", term)));
		}

		
		int i=0;

		//SpanNearQuery sq = new SpanNearQuery(phrase.toArray(new SpanQuery[phrase.size()]), 2, true);
		//sq.setBoost(100);
		//mainBooleanQuery.add(sq, BooleanClause.Occur.SHOULD);
		TopDocs search = searcher.search(mainBooleanQuery, num);
        if (search.totalHits > 0 ) {
        	//Map <Integer, String> results = new TreeMap<>();
        	ScoreDoc docs[] = (search.scoreDocs);
        	for (ScoreDoc sc : docs ){
        		Document doc = searcher.doc(sc.doc);
        		companies.add(new Company(doc.get("city"), doc.get("name"), doc.get("state"), doc.get("address"),  doc.get("fein")));
        		//results.put(Integer.valueOf(doc.get("fein")),doc.get("name") );
        		//, docs.scoreDocs[0].score, null,
        		if (companies.size()>num) break;
        	}
        //	searcher.close();
        	return companies;
        	
        }
       // searcher.close();
        return null;


		
		
	}
	
	public TreeSet<Company> searchByFein(String fein, int num) throws IOException{
		TreeSet<Company> companies=new TreeSet<>();
		Query q = new TermQuery(new Term("fein", fein));
		TopDocs search = searcher.search(q, num);
        if (search.totalHits > 0 ) {
        	//Map <Integer, String> results = new TreeMap<>();
        	ScoreDoc docs[] = (search.scoreDocs);
        	for (ScoreDoc sc : docs ){
        		Document doc = searcher.doc(sc.doc);
        		companies.add(new Company(doc.get("city"), doc.get("name"), doc.get("state"), doc.get("address"),  doc.get("fein")));
        		//results.put(Integer.valueOf(doc.get("fein")),doc.get("name") );
        		//, docs.scoreDocs[0].score, null,
        		if (companies.size()>num) break;
        	}
        //	searcher.close();
        	return companies;
        }
       // searcher.close();
        return null;

		
	}
	
}

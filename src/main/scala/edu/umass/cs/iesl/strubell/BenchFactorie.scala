package edu.umass.cs.iesl.strubell

import edu.stanford.nlp.tagger.maxent.MaxentTagger
import java.io.File
import cc.factorie._
import cc.factorie.app.nlp._
import cc.factorie.app.nlp.load._
import scala.io._

object BenchFactorie {

  var RANDOM_SEED = 0
  var DEFAULT_TAGGER_SAVENAME = "NO-NAME.tagger"
  
  def main(args: Array[String]) {

    var trainDir = ""
    var testDir = ""
    if (args.size > 0) {
      testDir = args(0)
    } else {
      println("Not going to bother running with no test data. Exiting")
      System.exit(0)
    }
    if (args.size > 1) {
      trainDir = args(1)
    }
    
    trainDir = "/Users/strubell/Documents/research/data/ontotest/train/"
    testDir = "/Users/strubell/Documents/research/data/ontotest/dev/"

    println("Loading file lists...")
    //var testFileList = getFileListFromDir(testDir, "pmd")
    var testFileList = getFileListFromDir(testDir, "dep.2")
    var trainFileList = getFileListFromDir(trainDir, "pmd")

    println("Loading documents...")
    val testDocs = testFileList.map(LoadOntonotes5.fromFilename(_).head)
    val trainDocs = trainFileList.map(LoadOntonotes5.fromFilename(_).head)

    println("Getting sentences from documents...")
    val testSentences = testDocs.map(_.sentences).flatten
    val trainSentences = trainDocs.map(_.sentences).flatten

    var numRuns = 10
    var taggerFileName = "ontonotes-vanilla.tagger"
    
//    trainPOSTagger(trainSentences, testSentences, true, taggerFileName)
//    
//    val serializedTagger = new pos.POS1
//    serializedTagger.deserialize(new File(taggerFileName))
//    
//    testPOSTagger(serializedTagger, testSentences, numRuns)
    
    testNER(testDocs, numRuns)
    //testDP(testSentences, numRuns) 
  }

  /**
   * Train POS1 on the given training data
   * @param trainSentences sentences to train on
   * @param testSentences sentences to test on during training
   * @param serialize whether to serialize the tagger
   * @param saveName filename to save the tagger to if serializing
   */
  def trainPOSTagger(trainSentences: Seq[Sentence], testSentences: Seq[Sentence], serialize: Boolean = false, saveName: String = DEFAULT_TAGGER_SAVENAME) {
    val tagger = new pos.POS1
    println("Training POS1 tagger...")
    implicit val rng = new scala.util.Random(RANDOM_SEED)
    tagger.train(trainSentences, testSentences)
    if(serialize){
	  println("Serializing POS1 tagger to file " + saveName + " ...")
	  tagger.serialize(saveName)
    }
    tagger
  }

  def testPOSTagger(tagger: pos.POS1, testSentences: Seq[Sentence], numRuns: Integer) = {
	var tokSpeedTotal = 0.0
	var tokAccuracy = 0.0
	var sentAccuracy = 0.0
    for (i <- 1 to numRuns) {
      implicit val rng = new scala.util.Random(RANDOM_SEED)
      var(tokenAccuracy, sentenceAccuracy, speed) = tagger.detailedAccuracy(testSentences)
      tokSpeedTotal += speed
      if(i == numRuns){
    	tokAccuracy = tokenAccuracy
    	sentAccuracy = sentenceAccuracy
      }
    }
	println("Average speed over " + numRuns + " trials: " + tokSpeedTotal/numRuns)
	println("Sentence accuracy: " + sentAccuracy)
	println("Token accuracy: " + tokAccuracy)
  }

  def testNER(testDocs: Seq[Document], numRuns: Integer) = {

    val namedent = ner.NER3

    println("Testing named entity recognition...")
    var sentSpeedTotal = 0.0
    var tokSpeedTotal = 0.0
    for (i <- 1 to numRuns) {
      implicit val rng = new scala.util.Random(RANDOM_SEED)
      namedent.printEvaluation(testDocs)
      val (sentPerSec, tokPerSec) = namedent.detailedAccuracy(testDocs)
      sentSpeedTotal += sentPerSec
      tokSpeedTotal += tokPerSec
      //println("accuracy: " + accuracy)
      println("Sentences/sec: " + sentPerSec)
      println("Tokens/sec: " + tokPerSec)
      println()
    }
    println("Average speed over " + numRuns + " trials: " + sentSpeedTotal/numRuns + " sents/sec " + tokSpeedTotal + "toks/sec")
  }

  def testDP(testSentences: Seq[Sentence], numRuns: Integer) = {
    
    val dp = parse.DepParser1Ontonotes 

    println("Testing dependency parser...")
    for (i <- 1 to numRuns) {
      implicit val rng = new scala.util.Random(RANDOM_SEED)
      val(las, uas, sentPerSec, tokPerSec) = dp.testSingle(testSentences)
      println("LAS: " + las)
      println("UAS: " + uas)
      println("Sentences/sec: " + sentPerSec)
      println("Tokens/sec: " + tokPerSec)
    }
  }

  /**
   * Returns a list of the file names of files with the given ending under the given directory
   */
  def getFileListFromDir(fileName: String, ending: String): Seq[String] = {
    val dir = new File(fileName)
    println("Getting file list from directory: " + fileName)
    if (dir != null) {
      dir.listFiles.filter(_.getName.endsWith(ending)).map(_.getAbsolutePath)
    } else {
      println("Directory not found: " + fileName)
      null
    }
  }

  /**
   * Time the given block in nanoseconds, printing the time and returning the result of the block
   */
  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }
}

package edu.umass.cs.iesl.strubell

import edu.stanford.nlp.tagger.maxent.MaxentTagger
import java.io.File
import java.net.URL
import cc.factorie._
import cc.factorie.app.nlp._
import cc.factorie.app.nlp.load._
import scala.io._
import cc.factorie.app.nlp.ner._
import cc.factorie.app.nlp.embeddings._

object BenchPOS {

  var RANDOM_SEED = 0
  var DEFAULT_TAGGER_SAVENAME = "NO-NAME.tagger"

  def main(args: Array[String]) {

    var whichTagger = args(0)
    var testDir = args(1)
    var trainDir = args(2)

    println("Loading tagger...")
    val taggerLoc = args(1)
    val tagger = new pos.ForwardPOSTagger
    tagger.deserialize(new java.io.File(taggerLoc))

    println("Loading file lists...")
    var testFileList = getFileListFromDir(testDir, "pmd")
    var trainFileList = getFileListFromDir(trainDir, "pmd")
    //var testFileList = getFileListFromDir(testDir, "dep.2")

    println("Loading documents...")
    val testDocs = testFileList.map(LoadOntonotes5.fromFilename(_).head)
    val trainDocs = trainFileList.map(LoadOntonotes5.fromFilename(_).head)

    println("Getting sentences from documents...")
    val testSentences = testDocs.map(_.sentences).flatten
    val trainSentences = trainDocs.map(_.sentences).flatten

    var numRuns = 10

    var results = for (i <- 1 to numRuns) yield { tagger.detailedAccuracy(testSentences) }

    var tokSpeed = results.map(_._3).sum / numRuns
    var tokAccuracy = results.map(_._1).sum / numRuns
    var sentAccuracy = results.map(_._2).sum / numRuns

    println("Average speed over " + numRuns + " trials: " + tokSpeed)
    println("Sentence accuracy: " + sentAccuracy)
    println("Token accuracy: " + tokAccuracy)
  }

  /**
   * Returns a list of the file names of files with the given ending under the given directory
   */
  def getFileListFromDir(fileName: String, ending: String = ""): Seq[String] = {
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

object BenchNER {

  var RANDOM_SEED = 0

  def main(args: Array[String]) {

    val conllTestFile = args(0)
    val modelLoc = args(1)
    var numRuns = 10
    val conllTestDoc = LoadConll2003(BILOU = true).fromFilename(conllTestFile)
    testNER(modelLoc, conllTestDoc, numRuns)
  }

  def testNER(modelLoc: String, testDocs: Seq[Document], numRuns: Integer) = {
    val modelURL = new java.net.URL("file://" + modelLoc)
    val namedent = new ner.StackedConllNER(SkipGramEmbedding, 100, 1.0, true, modelURL)
    println("Testing named entity recognition...")

    // throw away first one
    testDocs.foreach(namedent.process)
    namedent.test(testDocs)

    var results = for (i <- 1 to numRuns) yield { namedent.test(testDocs) }

    var sentSpeed = results.map(_._1).sum / numRuns
    var tokSpeed = results.map(_._2).sum / numRuns
    var f1 = results.map(_._3).sum / numRuns

    println("Average speed over " + numRuns + " trials: " + sentSpeed + " sents/sec " + tokSpeed + "toks/sec")
    println("F1: " + f1)
  }
}

object BenchDP {

  var RANDOM_SEED = 0

  def main(args: Array[String]) {

    val testFile = args(0)
    val modelLoc = args(1)
    val testDoc = LoadOntonotes5.fromFilename(testFile)
    var numRuns = 10
    
    val dp = new parse.TransitionParser(new java.net.URL("file://" + modelLoc))
    
    testDP(dp, testDoc.flatMap(_.sentences), numRuns)
  }

  def testDP(dp: parse.TransitionParser, testSentences: Seq[Sentence], numRuns: Integer) = {

    println("Testing dependency parsing...")

    var results = for (i <- 1 to numRuns) yield { dp.test(testSentences) }

    var las = results.map(_._1).sum / numRuns
    var uas = results.map(_._2).sum / numRuns
    var tokSpeed = results.map(_._3).sum / numRuns
    var sentSpeed = results.map(_._4).sum / numRuns

    println("Average speed over " + numRuns + " trials: " + sentSpeed + " sents/sec " + tokSpeed + "toks/sec")
    println("LAS: " + las + "; UAS: " + uas)
  }
}

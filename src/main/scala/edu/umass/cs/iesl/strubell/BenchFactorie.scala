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
import cc.factorie.util.FileUtils

object BenchPOS {

  var RANDOM_SEED = 0
  var DEFAULT_TAGGER_SAVENAME = "NO-NAME.tagger"

  def main(args: Array[String]) {

    var modelLoc = args(0)
    var testDir = args(1)

    println("Loading tagger...")
    val tagger = new pos.ForwardPosTagger
    tagger.deserialize(new java.io.File(modelLoc))

    println("Loading file lists...")
    var testFileList = FileUtils.getFileListFromDir(testDir, "pmd")

    println("Loading documents...")
    val testDocs = testFileList.map(LoadOntonotes5.fromFilename(_).head)

    println("Getting sentences from documents...")
    val testSentences = testDocs.flatMap(_.sentences)

    var numRuns = 10

    var results = for (i <- 1 to numRuns) yield { tagger.accuracy(testSentences) }

    var tokSpeed = results.map(_._3).sum / numRuns
    var tokAccuracy = results.map(_._1).sum / numRuns
    var sentAccuracy = results.map(_._2).sum / numRuns

    println("Average speed over " + numRuns + " trials: " + tokSpeed + " toks/sec")
    println("Sentence accuracy: " + sentAccuracy)
    println("Token accuracy: " + tokAccuracy)
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

    val modelLoc = args(0)
    val conllTestFile = args(1)
    var numRuns = 10
    testNER(modelLoc, Seq(conllTestFile), numRuns)
  }

  def testNER(modelLoc: String, testFiles: Seq[String], numRuns: Integer) = {
    val modelURL = new java.net.URL("file://" + modelLoc)
    val namedent = new ner.ConllStackedChainNer(SkipGramEmbedding, 100, 1.0, true, modelURL)
    println("Testing named entity recognition...")

    // throw away first one
    //testDocs.foreach(namedent.process)
    //namedent.test(testDocs)

    // load a new document every time to get accurate timing
    var results = for (i <- 1 to numRuns) yield { namedent.test(testFiles.flatMap(LoadConll2003(BILOU = true).fromFilename(_))) }

    var sentSpeed = results.map(_._1).sum / numRuns
    var tokSpeed = results.map(_._2).sum / numRuns
    var f1 = results.map(_._3).sum / numRuns

    println("Average speed over " + numRuns + " trials: " + sentSpeed + " sents/sec " + tokSpeed + " toks/sec")
    println("F1: " + f1)
  }
}

object BenchDP {

  var RANDOM_SEED = 0

  def main(args: Array[String]) {

    val modelLoc = args(0)
    val testDir = args(1)
    
    println("Loading file lists...")
    var testFileList = FileUtils.getFileListFromDir(testDir, "pmd")

    println("Loading documents...")
    val testDocs = testFileList.map(LoadOntonotes5.fromFilename(_).head)
    
    //val testDoc = LoadOntonotes5.fromFilename(testFile)
    var numRuns = 1
    
    val dp = new parse.TransitionBasedParser(new java.net.URL("file://" + modelLoc))
    //val dp = parse.OntonotesTransitionBasedParser
    
    testDP(dp, testDocs.flatMap(_.sentences), numRuns)
  }

  def testDP(dp: parse.TransitionBasedParser, testSentences: Seq[Sentence], numRuns: Integer) = {

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

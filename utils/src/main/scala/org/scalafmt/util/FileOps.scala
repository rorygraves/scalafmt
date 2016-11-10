package org.scalafmt.util

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.io.Reader

object FileOps {

  def makeAbsolute(workingDir: File)(file: File): File =
    if (file.isAbsolute) file
    else new File(workingDir, file.getPath)

  def listFiles(path: String): Vector[String] = {
    listFiles(new File(path))
  }

  def listFiles(file: AbsoluteFile): Vector[AbsoluteFile] = {
    listFiles(file.jfile).map(x => AbsoluteFile.fromFile(new File(x), file))
  }

  def listFiles(file: File): Vector[String] = {
    if (file.isFile) {
      Vector(file.getAbsolutePath)
    } else {
      def listFilesIter(s: File): Iterable[String] = {
        val (dirs, files) = Option(s.listFiles()).toIterable
          .flatMap(_.toIterator)
          .partition(_.isDirectory)
        files.map(_.getPath) ++ dirs.flatMap(listFilesIter)
      }
      for {
        f0 <- Option(listFilesIter(file)).toVector
        filename <- f0
      } yield filename
    }
  }

  // TODO(olafur) allow user to specify encoding through CLI.
  /**
    * Reads file from file system or from http url.
    */
  def readFile(filename: String): String = {
    if (filename matches "https?://.*") {
      scala.io.Source.fromURL(filename)("UTF-8").getLines().mkString("\n")
    } else {
      readFile(new File(filename))
    }
  }

  def readFile(file: AbsoluteFile): String = {
    readFile(file.jfile)
  }

  def readFile(file: File): String = {
    readFile(new BufferedReader(new FileReader(file)))
  }
  val lineSeparator: String = System.getProperty("line.separator")

  def readFile(br: BufferedReader): String = {
    // Prefer this to inefficient Source.fromFile.
    val sb = new StringBuilder
    try {
      var line = ""
      while ({
        line = br.readLine()
        line != null
      }) {
        sb.append(line)
        sb.append(lineSeparator)
      }
    } finally {
      br.close()
    }
    sb.toString()
  }

  def getFile(path: String*): File = {
    new File(path.mkString(File.separator))
  }

  def writeFile(file: File, content: String): Unit = {
    // For java 6 compatibility we don't use java.nio.
    val pw = new PrintWriter(file)
    try {
      pw.write(content)
    } finally {
      pw.close()
    }

  }
  def writeFile(filename: String, content: String): Unit = {
    writeFile(new File(filename), content)
  }
}

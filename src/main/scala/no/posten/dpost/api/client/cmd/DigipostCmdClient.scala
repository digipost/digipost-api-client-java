/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.posten.dpost.api.client.cmd
import no.posten.dpost.api.client.DigipostClient
import no.posten.dpost.api.client.representations.Recipient
import no.posten.dpost.api.client.DigipostClientException
import java.io.File
import scala.io.Source
import java.io.FileInputStream
import com.sun.jersey.api.client.filter.LoggingFilter
import no.posten.dpost.api.client.representations.Message
import no.posten.dpost.api.client.representations.DigipostAddress
import no.posten.dpost.api.client.representations.PersonalIdentificationNumber
import no.posten.dpost.api.client.representations.AuthenticationLevel.PASSWORD

object DigipostCmdClient extends App {
  val config = loadConfig()
  val options = parseOptions(args.toList)

  val messageFiles = options("brev").split(',')
  val certStream = new FileInputStream(new File(config("sertifikat")))
  val client = new DigipostClient(config("endpoint"), config("brukerid").toLong, certStream, config("sertifikatpassord"))
  if (options.contains("debug")) client.addFilter(new LoggingFilter())

  try {
    val buildMessage = getMessageBuilder(options.keys.toList)
    messageFiles.foreach((brev: String) => {
      val brevStream = new FileInputStream(new File(brev))
      val response = client.sendMessage(buildMessage(System.currentTimeMillis().toString(), options("emne")), brevStream)
      brevStream.close()
    })
  } catch {
    case ex: DigipostClientException => println(ex.getErrorMessage())
    case ex2 => println(ex2)
  }

  def messageWithPersonalIdentificationNumber(messageId: String, subject: String, personalIdentificationNumber: PersonalIdentificationNumber): Message = {
    new Message(messageId, subject, personalIdentificationNumber, false, PASSWORD)
  }

  def getMessageBuilder(keys: List[String]): (String, String) => Message = keys match {
    case "digipostadresse" :: tail => (messageId: String, subject: String) =>
      new Message(messageId, subject, new DigipostAddress(options("digipostadresse")), false, PASSWORD)
    case "fødselsnummer" :: tail => (messageId: String, subject: String) =>
      new Message(messageId, subject, new PersonalIdentificationNumber(options("fødselsnummer")), false, PASSWORD)
    case other :: tail => getMessageBuilder(tail)
    case Nil =>
      println("Ingen mottaker spesifisert")
      exit(1)
  }

  def loadConfig(): Map[String, String] = {
    val configFile = "./bin/config.properties"
    val ConfigLine = """^(.*?)=(.*)$""".r
    Source.fromFile(configFile).getLines().map {
      case ConfigLine(key, value) => Map(key -> value)
      case _ => Map()
    }.flatten.toMap
  }

  def parseOptions(args: List[String]): Map[String, String] = {
    val usage = """Usage: dpost-api-cmd [--debug] [--digipostadresse] [--fødselsnummer] emne brevfil[,brevfil,brevfil,...]"""
    if (args.length == 0) println(usage)

    def nextOption(list: List[String]): Map[String, String] = list match {
      case Nil => Map()
      case "--digipostadresse" :: value :: tail => Map("digipostadresse" -> value) ++ nextOption(tail)
      case "--fødselsnummer" :: value :: tail => Map("fødselsnummer" -> value) ++ nextOption(tail)
      case "--debug" :: tail => Map("debug" -> "true") ++ nextOption(tail)
      case emne :: brev :: Nil => Map("emne" -> emne, "brev" -> brev)
      case option :: tail =>
        println("Ugyldig parameter " + option)
        println(usage)
        exit(1)
    }
    nextOption(args)
  }
}

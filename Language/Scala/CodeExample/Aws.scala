#

import java.io.{InputStreamReader, BufferedReader}

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{S3ObjectSummary, ListObjectsRequest, Bucket}
import com.ucar.growth.analysis.common.AWSCredentialsUtil

import scala.collection.mutable
import scala.collection.mutable.HashMap

object S3Ops {
  def listSubFolder(client: AmazonS3,bucket: String, prefix: String) = {
    val request = new ListObjectsRequest()
      .withBucketName(bucket)
      .withPrefix(prefix)
    val response = client.listObjects(request)
    val root = new HashMap[String, HashMap[_, _] ]

    response.getObjectSummaries.toArray.foreach(obj => {
      val key = obj.asInstanceOf[S3ObjectSummary].getKey
      if (!key.contains('$')) {
        println(key)
        val path = key.split('/')

        if (path.length > 0) {
          var temp = root
          path.foreach(str => {
            if (!temp.contains(str)) {
              println("in and new ")
              temp.put(str, new HashMap[String, HashMap[_, _] ])
            }
            println("current path is : " + str)
            temp = temp.get(str).get.asInstanceOf[HashMap[String, HashMap[_, _] ] ]
          })
        }
      }
    })

    println("over")
  }

  def getAllObjects(client: AmazonS3, bucket: String) = {
    val seconds = client.listObjects(bucket).getObjectSummaries
    val iterator = seconds.iterator()
    if (iterator.hasNext) {
      println("Object: " + iterator.next().getKey)
    }
  }

  def openS3File(client: AmazonS3, bucket: String, path: String) = {
    val obj = client.getObject(bucket, path)
    val stream = obj.getObjectContent
    val lines = scala.io.Source.fromInputStream(stream).getLines
    lines.foreach(println)
  }

  def main(args: Array[String]) {
    val client = AWSCredentialsUtil.retrieveS3Client
    //openS3File(client, "growth-data-analysis", "jun/inputOrder/part-0")

    //get all buckets
    val list = client.listBuckets
    list.toArray.foreach(bucket => println(bucket.asInstanceOf[Bucket].getName))
    val iterator = list.iterator()
    while (iterator.hasNext) {
      val bucket = iterator.next()
      val name = bucket.getName
      //println("bucket: " + name)

      //special ops for growth bucket.
      if (name == "growth-data-analysis") listSubFolder(client, name, "jun")
    }

  }
}


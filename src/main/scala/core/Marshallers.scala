package core

import akka.http.scaladsl.marshalling.{PredefinedToEntityMarshallers, ToEntityMarshaller}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

trait ProtobufMarshalling[T <: GeneratedMessage, E <: GeneratedMessage] {
  implicit def protobufMarshaller: ToEntityMarshaller[E] = PredefinedToEntityMarshallers.ByteArrayMarshaller.compose[E](r => r.toByteArray)

  implicit def protobufUnmarshaller(implicit companion: GeneratedMessageCompanion[T]): FromEntityUnmarshaller[T] = {
    Unmarshaller.byteArrayUnmarshaller.map[T](bytes => companion.parseFrom(bytes))
  }
}


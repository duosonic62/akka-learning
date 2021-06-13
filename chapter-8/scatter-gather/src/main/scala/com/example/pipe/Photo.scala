 package com.example.pipe

import java.time.LocalDateTime

case class PhotoMessage(id: String, photo: String, creationTIme: Option[LocalDateTime] = None, speed: Option[Int] = None)

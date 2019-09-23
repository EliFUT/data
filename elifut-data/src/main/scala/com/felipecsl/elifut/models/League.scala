package com.felipecsl.elifut.models

case class League(
  imageUrls: Image,
  abbrName: String,
  id: Int,
  imgUrl: Option[String],
  name: String
)

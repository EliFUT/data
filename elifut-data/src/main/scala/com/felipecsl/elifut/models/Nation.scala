package com.felipecsl.elifut.models

case class Nation(
  imageUrls: SizedImage,
  abbrName: String,
  id: Int,
  imgUrl: Option[String],
  name: String
)

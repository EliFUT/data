package com.felipecsl.elifut.models

case class Club(
  imageUrls: ClubImage,
  abbrName: String,
  id: Int,
  imgUrl: Option[String],
  name: String
)

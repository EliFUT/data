package com.felipecsl.elifut

case class ItemsResponse(
  page: Int,
  totalPages: Int,
  totalResults: Int,
  `type`: String,
  count: Int,
  items: Seq[Player]
)

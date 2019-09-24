package com.felipecsl.elifut

import com.felipecsl.elifut.models.Player

case class ItemsResponse(
  page: Int,
  totalPages: Int,
  totalResults: Int,
  `type`: String,
  count: Int,
  items: Seq[Player]
)

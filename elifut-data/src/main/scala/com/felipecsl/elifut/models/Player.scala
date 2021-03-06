package com.felipecsl.elifut.models

case class Player(
  commonName: Option[String],
  firstName: String,
  lastName: String,
  league: League,
  nation: Nation,
  club: Club,
  headshot: Headshot,
  position: String,
  composure: Int,
  playStyle: String,
  playStyleId: Option[String],
  height: Int,
  weight: Int,
  birthdate: String,
  age: Int,
  acceleration: Int,
  aggression: Int,
  agility: Int,
  balance: Int,
  ballcontrol: Int,
  foot: String,
  skillMoves: Int,
  crossing: Int,
  curve: Int,
  dribbling: Int,
  finishing: Int,
  freekickaccuracy: Int,
  gkdiving: Int,
  gkhandling: Int,
  gkkicking: Int,
  gkpositioning: Int,
  gkreflexes: Int,
  headingaccuracy: Int,
  interceptions: Int,
  jumping: Int,
  longpassing: Int,
  longshots: Int,
  marking: Int,
  penalties: Int,
  positioning: Int,
  potential: Int,
  reactions: Int,
  shortpassing: Int,
  shotpower: Int,
  slidingtackle: Int,
  sprintspeed: Int,
  standingtackle: Int,
  stamina: Int,
  strength: Int,
  vision: Int,
  volleys: Int,
  weakFoot: Int,
  traits: Seq[String],
  specialities: Seq[String],
  atkWorkRate: String,
  defWorkRate: String,
  playerType: Option[String],
  attributes: Seq[Attribute],
  name: String,
  rarityId: Int,
  isIcon: Boolean,
  quality: String,
  isGK: Boolean,
  positionFull: String,
  isSpecialType: Boolean,
  contracts: Option[String],
  fitness: Option[String],
  rawAttributeChemistryBonus: Option[String],
  isLoan: Option[Boolean],
  squadPosition: Option[String],
  iconAttributes: Option[IconAttributes],
  itemType: String,
  discardValue: Option[String],
  id: String,
  modelName: String,
  baseId: Long,
  rating: Int
)

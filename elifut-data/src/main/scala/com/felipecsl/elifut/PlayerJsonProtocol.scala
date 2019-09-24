package com.felipecsl.elifut

import com.felipecsl.elifut.models.{Attribute, Club, ClubImage, Headshot, IconAttributes, Image, League, Nation, Player, SizedImage, TeamStats}
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

object PlayerJsonProtocol extends DefaultJsonProtocol {
  implicit object PlayerJsonFormat extends RootJsonFormat[Player] {
    private implicit val imageJsonFormat: RootJsonFormat[Image] = jsonFormat2(Image)
    private implicit val sizedImageJsonFormat: RootJsonFormat[SizedImage] = jsonFormat3(SizedImage)
    private implicit val nationJsonFormat: RootJsonFormat[Nation] = jsonFormat5(Nation)
    private implicit val leagueJsonFormat: RootJsonFormat[League] = jsonFormat5(League)
    private implicit val headshotJsonFormat: RootJsonFormat[Headshot] = jsonFormat2(Headshot)
    private implicit val clubImageJsonFormat: RootJsonFormat[ClubImage] = jsonFormat2(ClubImage)
    private implicit val clubJsonFormat: RootJsonFormat[Club] = jsonFormat5(Club)
    private implicit val attributeJsonFormat: RootJsonFormat[Attribute] = jsonFormat3(Attribute)
    private implicit val teamStatsJsonFormat: RootJsonFormat[TeamStats] = jsonFormat5(TeamStats)
    private implicit val iconAttributesJsonFormat: RootJsonFormat[IconAttributes] = jsonFormat3(IconAttributes)

    def read(value: JsValue): Player = {
      val fields = value.asJsObject.getFields(
        "commonName", "firstName", "lastName", "league", "nation", "club", "headshot", "position",
        "composure", "playStyle", "playStyleId", "height", "weight", "birthdate", "age",
        "acceleration", "aggression", "agility", "balance", "ballcontrol", "foot", "skillMoves",
        "crossing", "curve", "dribbling", "finishing", "freekickaccuracy", "gkdiving", "gkhandling",
        "gkkicking", "gkpositioning", "gkreflexes", "headingaccuracy", "interceptions", "jumping",
        "longpassing", "longshots", "marking", "penalties", "positioning", "potential", "reactions",
        "shortpassing", "shotpower", "slidingtackle", "sprintspeed", "standingtackle", "stamina",
        "strength", "vision", "volleys", "weakFoot", "traits", "specialities", "atkWorkRate",
        "defWorkRate", "playerType", "attributes", "name", "rarityId", "isIcon", "quality",
        "isGK", "positionFull", "isSpecialType", "contracts", "fitness", "rawAttributeChemistryBonus",
        "isLoan", "squadPosition", "iconAttributes", "itemType", "discardValue", "id", "modelName",
        "baseId", "rating"
      )

      val commonName = fields.head match {
        case JsString.empty => None
        case JsString(commonName) => Some(commonName)
      }
      val firstName = fields(1) match { case JsString(firstName) => firstName }
      val lastName = fields(2) match { case JsString(lastName) => lastName }
      val league = fields(3) match { case JsObject(league) => league }
      val nation = fields(4) match { case JsObject(nation) => nation }
      val club = fields(5) match { case JsObject(club) => club }
      val headshot = fields(6) match { case JsObject(headshot) => headshot }
      val position = fields(7) match { case JsString(position) => position }
      val composure = fields(8) match { case JsNumber(composure) => composure }
      val playStyle = fields(9) match { case JsString(playStyle) => playStyle }
      val playStyleId = fields(10) match {
        case JsString(playStyleId) => playStyleId
        case JsNull => null
      }
      val height = fields(11) match { case JsNumber(height) => height }
      val weight = fields(12) match { case JsNumber(weight) => weight }
      val birthdate = fields(13) match { case JsString(birthdate) => birthdate }
      val age = fields(14) match { case JsNumber(age) => age }
      val acceleration = fields(15) match { case JsNumber(acceleration) => acceleration }
      val aggression = fields(16) match { case JsNumber(aggression) => aggression }
      val agility = fields(17) match { case JsNumber(agility) => agility }
      val balance = fields(18) match { case JsNumber(balance) => balance }
      val ballcontrol = fields(19) match { case JsNumber(ballcontrol) => ballcontrol }
      val foot = fields(20) match { case JsString(foot) => foot }
      val skillMoves = fields(21) match { case JsNumber(skillMoves) => skillMoves }
      val crossing = fields(22) match { case JsNumber(crossing) => crossing }
      val curve = fields(23) match { case JsNumber(curve) => curve }
      val dribbling = fields(24) match { case JsNumber(dribbling) => dribbling }
      val finishing = fields(25) match { case JsNumber(finishing) => finishing }
      val freekickaccuracy = fields(26) match { case JsNumber(freekickaccuracy) => freekickaccuracy }
      val gkdiving = fields(27) match { case JsNumber(gkdiving) => gkdiving }
      val gkhandling = fields(28) match { case JsNumber(gkhandling) => gkhandling }
      val gkkicking = fields(29) match { case JsNumber(gkkicking) => gkkicking }
      val gkpositioning = fields(30) match { case JsNumber(gkpositioning) => gkpositioning }
      val gkreflexes = fields(31) match { case JsNumber(gkreflexes) => gkreflexes }
      val headingaccuracy = fields(32) match { case JsNumber(headingaccuracy) => headingaccuracy }
      val interceptions = fields(33) match { case JsNumber(interceptions) => interceptions }
      val jumping = fields(34) match { case JsNumber(jumping) => jumping }
      val longpassing = fields(35) match { case JsNumber(longpassing) => longpassing }
      val longshots = fields(36) match { case JsNumber(longshots) => longshots }
      val marking = fields(37) match { case JsNumber(value) => value }
      val penalties = fields(38) match { case JsNumber(value) => value }
      val positioning = fields(39) match { case JsNumber(value) => value }
      val potential = fields(40) match { case JsNumber(value) => value }
      val reactions = fields(41) match { case JsNumber(value) => value }
      val shortpassing = fields(42) match { case JsNumber(value) => value }
      val shotpower = fields(43) match { case JsNumber(value) => value }
      val slidingtackle = fields(44) match { case JsNumber(value) => value }
      val sprintspeed = fields(45) match { case JsNumber(value) => value }
      val standingtackle = fields(46) match { case JsNumber(value) => value }
      val stamina = fields(47) match { case JsNumber(value) => value }
      val strength = fields(48) match { case JsNumber(value) => value }
      val vision = fields(49) match { case JsNumber(value) => value }
      val volleys = fields(50) match { case JsNumber(value) => value }
      val weakFoot = fields(51) match { case JsNumber(value) => value }
      val traits = fields(52) match {
        case JsArray(value) => value
        case JsNull => null
      }
      val specialities = fields(53) match {
        case JsArray(specialities) => specialities
        case JsNull => null
      }
      val atkWorkRate = fields(54) match { case JsString(atkWorkRate) => atkWorkRate }
      val defWorkRate = fields(55) match { case JsString(defWorkRate) => defWorkRate }
      val playerType = fields(56) match {
        case JsString(playerType) => playerType
        case JsNull => null
      }
      val attributes = fields(57) match { case JsArray(attributes) => attributes }
      val name = fields(58) match { case JsString(name) => name }
      val rarityId = fields(59) match { case JsNumber(rarityId) => rarityId }
      val isIcon = fields(60) match { case JsBoolean(isIcon) => isIcon }
      val quality = fields(61) match { case JsString(quality) => quality }
      val isGK = fields(62) match { case JsBoolean(isGK) => isGK }
      val positionFull = fields(63) match { case JsString(positionFull) => positionFull }
      val isSpecialType = fields(64) match { case JsBoolean(isSpecialType) => isSpecialType }
      val contracts = fields(65) match {
        case JsString(contracts) => Some(contracts)
        case JsNull => None
      }
      val fitness = fields(66) match {
        case JsString(fitness) => Some(fitness)
        case JsNull => None
      }
      val rawAttributeChemistryBonus = fields(67) match {
        case JsString(rawAttributeChemistryBonus) => Some(rawAttributeChemistryBonus)
        case JsNull => None
      }
      val isLoan = fields(68) match {
        case JsBoolean(isLoan) => Some(isLoan)
        case JsNull => None
      }
      val squadPosition = fields(69) match {
        case JsString(squadPosition) => Some(squadPosition)
        case JsNull => None
      }
      val iconAttributes = fields(70) match {
        case obj@JsObject(_) => Some(obj)
        case JsNull => None
      }
      val itemType = fields(71) match { case JsString(itemType) => itemType }
      val discardValue = fields(72) match {
        case JsString(discardValue) => Some(discardValue)
        case JsNull => None
      }
      val id = fields(73) match { case JsString(id) => id }
      val modelName = fields(74) match { case JsString(modelName) => modelName }
      val baseId = fields(75) match { case JsNumber(baseId) => baseId }
      val rating = fields(76) match { case JsNumber(rating) => rating }
      Player(
        commonName,
        firstName,
        lastName,
        JsObject(league).convertTo[League],
        JsObject(nation).convertTo[Nation],
        JsObject(club).convertTo[Club],
        JsObject(headshot).convertTo[Headshot],
        position,
        composure.toInt,
        playStyle,
        Option(playStyleId),
        height.toInt,
        weight.toInt,
        birthdate,
        age.toInt,
        acceleration.toInt,
        aggression.toInt,
        agility.toInt,
        balance.toInt,
        ballcontrol.toInt,
        foot,
        skillMoves.toInt,
        crossing.toInt,
        curve.toInt,
        dribbling.toInt,
        finishing.toInt,
        freekickaccuracy.toInt,
        gkdiving.toInt,
        gkhandling.toInt,
        gkkicking.toInt,
        gkpositioning.toInt,
        gkreflexes.toInt,
        headingaccuracy.toInt,
        interceptions.toInt,
        jumping.toInt,
        longpassing.toInt,
        longshots.toInt,
        marking.toInt,
        penalties.toInt,
        positioning.toInt,
        potential.toInt,
        reactions.toInt,
        shortpassing.toInt,
        shotpower.toInt,
        slidingtackle.toInt,
        sprintspeed.toInt,
        standingtackle.toInt,
        stamina.toInt,
        strength.toInt,
        vision.toInt,
        volleys.toInt,
        weakFoot.toInt,
        Option(traits).map(_.map(_.convertTo[String])).getOrElse(Vector.empty),
        Option(specialities).map(_.map(_.convertTo[String])).getOrElse(Vector.empty),
        atkWorkRate,
        defWorkRate,
        Option(playerType),
        attributes.map(_.convertTo[Attribute]),
        name,
        rarityId.toInt,
        isIcon,
        quality,
        isGK,
        positionFull,
        isSpecialType,
        contracts,
        fitness,
        rawAttributeChemistryBonus,
        isLoan,
        squadPosition,
        iconAttributes.map(_.convertTo[IconAttributes]),
        itemType,
        discardValue,
        id,
        modelName,
        baseId.toLong,
        rating.toInt
      )
    }

    override def write(obj: Player): JsValue = ???
  }
}
package com.hiscoreswatch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An enumeration of all skills, minigames, and bosses in the OSRS Hiscores API.
 * The order and indices are based on the official index_lite.ws API response.
 */
@Getter
@RequiredArgsConstructor
public enum Hiscores
{
    // Skills
    OVERALL("Overall", 0),
    ATTACK("Attack", 1),
    DEFENCE("Defence", 2),
    STRENGTH("Strength", 3),
    HITPOINTS("Hitpoints", 4),
    RANGED("Ranged", 5),
    PRAYER("Prayer", 6),
    MAGIC("Magic", 7),
    COOKING("Cooking", 8),
    WOODCUTTING("Woodcutting", 9),
    FLETCHING("Fletching", 10),
    FISHING("Fishing", 11),
    FIREMAKING("Firemaking", 12),
    CRAFTING("Crafting", 13),
    SMITHING("Smithing", 14),
    MINING("Mining", 15),
    HERBLORE("Herblore", 16),
    AGILITY("Agility", 17),
    THIEVING("Thieving", 18),
    SLAYER("Slayer", 19),
    FARMING("Farming", 20),
    RUNECRAFT("Runecraft", 21),
    HUNTER("Hunter", 22),
    CONSTRUCTION("Construction", 23),

    // Minigames (Clue Scrolls & Other)
    LEAGUE_POINTS("League Points", 24),
    BOUNTY_HUNTER_HUNTER("Bounty Hunter - Hunter", 25),
    BOUNTY_HUNTER_ROGUE("Bounty Hunter - Rogue", 26),
    CLUE_SCROLL_ALL("Clue Scrolls (All)", 27),
    CLUE_SCROLL_BEGINNER("Clue Scrolls (Beginner)", 28),
    CLUE_SCROLL_EASY("Clue Scrolls (Easy)", 29),
    CLUE_SCROLL_MEDIUM("Clue Scrolls (Medium)", 30),
    CLUE_SCROLL_HARD("Clue Scrolls (Hard)", 31),
    CLUE_SCROLL_ELITE("Clue Scrolls (Elite)", 32),
    CLUE_SCROLL_MASTER("Clue Scrolls (Master)", 33),
    LMS_RANK("LMS - Rank", 34),
    PVP_ARENA_RANK("PvP Arena - Rank", 35),
    SOUL_WARS_ZEAL("Soul Wars Zeal", 36),
    RIFTS_CLOSED("Rifts Closed", 37),

    // Bosses
    ABYSSAL_SIRE("Abyssal Sire", 38),
    ALCHEMICAL_HYDRA("Alchemical Hydra", 39),
    BARROWS_CHESTS("Barrows Chests", 40),
    BRYOPHYTA("Bryophyta", 41),
    CALLISTO("Callisto", 42),
    CERBERUS("Cerberus", 43),
    CHAMBERS_OF_XERIC("Chambers of Xeric", 44),
    CHAMBERS_OF_XERIC_CM("Chambers of Xeric: CM", 45),
    CHAOS_ELEMENTAL("Chaos Elemental", 46),
    CHAOS_FANATIC("Chaos Fanatic", 47),
    COMMANDER_ZILYANA("Commander Zilyana", 48),
    CORPOREAL_BEAST("Corporeal Beast", 49),
    CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", 50),
    DAGANNOTH_PRIME("Dagannoth Prime", 51),
    DAGANNOTH_REX("Dagannoth Rex", 52),
    DAGANNOTH_SUPREME("Dagannoth Supreme", 53),
    DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", 54),
    GENERAL_GRAARDOR("General Graardor", 55),
    GIANT_MOLE("Giant Mole", 56),
    GROTESQUE_GUARDIANS("Grotesque Guardians", 57),
    HESPORI("Hespori", 58),
    KALPHITE_QUEEN("Kalphite Queen", 59),
    KING_BLACK_DRAGON("King Black Dragon", 60),
    KRAKEN("Kraken", 61),
    KREE_ARRA("Kree'arra", 62),
    K_RIL_TSUTSAROTH("K'ril Tsutsaroth", 63),
    MIMIC("Mimic", 64),
    NEX("Nex", 65),
    NIGHTMARE("The Nightmare", 66),
    PHOSANIS_NIGHTMARE("Phosani's Nightmare", 67),
    OBOR("Obor", 68),
    SARACHNIS("Sarachnis", 69),
    SCORPIA("Scorpia", 70),
    SKOTIZO("Skotizo", 71),
    TEMPOROSS("Tempoross", 72),
    THE_GAUNTLET("The Gauntlet", 73),
    THE_CORRUPTED_GAUNTLET("The Corrupted Gauntlet", 74),
    THEATRE_OF_BLOOD("Theatre of Blood", 75),
    THEATRE_OF_BLOOD_HM("Theatre of Blood: HM", 76),
    THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear Smoke Devil", 77),
    TOMBS_OF_AMASCUT("Tombs of Amascut", 78),
    TOMBS_OF_AMASCUT_EXPERT("Tombs of Amascut: Expert", 79),
    TZKAL_ZUK("TzKal-Zuk", 80),
    TZTOK_JAD("TzTok-Jad", 81),
    VENENATIS("Venenatis", 82),
    VET_ION("Vet'ion", 83),
    VORKATH("Vorkath", 84),
    WINTERTODT("Wintertodt", 85),
    ZALCANO("Zalcano", 86),
    ZULRAH("Zulrah", 87);

    private final String name;
    /**
     * The index of the hiscore category in the API response (index_lite).
     */
    private final int apiIndex;

    @Override
    public String toString()
    {
        return name;
    }
}

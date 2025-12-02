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
    SAILING("Sailing",24),

    // Minigames (Clue Scrolls & Other)
    GRID_POINTS("Grid Points", 25),
    LEAGUE_POINTS("League Points", 26),
    DEADMAN_POINTS("Deadman Points", 27),
    BOUNTY_HUNTER_HUNTER("Bounty Hunter - Hunter", 28),
    BOUNTY_HUNTER_ROGUE("Bounty Hunter - Rogue", 29),
    BOUNTY_HUNTER_HUNTER_LEGACY("Bounty Hunter (Legacy) - Hunter", 30),
    BOUNTY_HUNTER_ROGUE_LEGACY("Bounty Hunter (Legacy) - Rogue", 31),
    CLUE_SCROLL_ALL("Clue Scrolls (All)", 32),
    CLUE_SCROLL_BEGINNER("Clue Scrolls (Beginner)", 33),
    CLUE_SCROLL_EASY("Clue Scrolls (Easy)", 34),
    CLUE_SCROLL_MEDIUM("Clue Scrolls (Medium)", 35),
    CLUE_SCROLL_HARD("Clue Scrolls (Hard)", 36),
    CLUE_SCROLL_ELITE("Clue Scrolls (Elite)", 37),
    CLUE_SCROLL_MASTER("Clue Scrolls (Master)", 38),
    LMS_RANK("LMS - Rank", 39),
    PVP_ARENA_RANK("PvP Arena - Rank", 40),
    SOUL_WARS_ZEAL("Soul Wars Zeal", 41),
    RIFTS_CLOSED("Rifts Closed", 42),
    COLOSSEUM_GLORY("Colosseum Glory", 43),
    COLLECTIONS_LOGGED("Collections Logged", 44),

    // Bosses
    ABYSSAL_SIRE("Abyssal Sire", 45),
    ALCHEMICAL_HYDRA("Alchemical Hydra", 46),
    AMOXLIATL("Amoxliatl", 47),
    ARAXXOR("Araxxor", 48),
    ARTIO("Artio", 49),
    BARROWS_CHESTS("Barrows Chests", 50),
    BRYOPHYTA("Bryophyta", 51),
    CALLISTO("Callisto", 52),
    CAL_VARION("Cal'varion", 53),
    CERBERUS("Cerberus", 54),
    CHAMBERS_OF_XERIC("Chambers of Xeric", 55),
    CHAMBERS_OF_XERIC_CM("Chambers of Xeric: CM", 56),
    CHAOS_ELEMENTAL("Chaos Elemental", 57),
    CHAOS_FANATIC("Chaos Fanatic", 58),
    COMMANDER_ZILYANA("Commander Zilyana", 59),
    CORPOREAL_BEAST("Corporeal Beast", 60),
    CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", 61),
    DAGANNOTH_PRIME("Dagannoth Prime", 62),
    DAGANNOTH_REX("Dagannoth Rex", 63),
    DAGANNOTH_SUPREME("Dagannoth Supreme", 64),
    DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", 65),
    DOOM_OF_MOKHAIOTL("Doom of Mokhaiotl", 66),
    DUKE_SUCELLUS("Duke Sucellus", 67),
    GENERAL_GRAARDOR("General Graardor", 68),
    GIANT_MOLE("Giant Mole", 69),
    GROTESQUE_GUARDIANS("Grotesque Guardians", 70),
    HESPORI("Hespori", 71),
    KALPHITE_QUEEN("Kalphite Queen", 72),
    KING_BLACK_DRAGON("King Black Dragon", 73),
    KRAKEN("Kraken", 74),
    KREE_ARRA("Kree'arra", 75),
    K_RIL_TSUTSAROTH("K'ril Tsutsaroth", 76),
    LUNAR_CHESTS("Lunar Chests", 77),
    MIMIC("Mimic", 78),
    NEX("Nex", 79),
    NIGHTMARE("The Nightmare", 80),
    PHOSANIS_NIGHTMARE("Phosani's Nightmare", 81),
    OBOR("Obor", 82),
    PHANTOM_MUSPAH("Phantom Muspah", 83),
    SARACHNIS("Sarachnis", 84),
    SCORPIA("Scorpia", 85),
    SCURRIUS("Scurrius", 86),
    SHELLBANE_GRYPHON("Shellbane Gryphon", 87),
    SKOTIZO("Skotizo", 88),
    SOL_HEREDIT("Sol Heredit", 89),
    SPINDEL("Spindel", 90),
    TEMPOROSS("Tempoross", 91),
    THE_GAUNTLET("The Gauntlet", 92),
    THE_CORRUPTED_GAUNTLET("The Corrupted Gauntlet", 93),
    THE_HUEYCOATL("The Hueycoatl", 94),
    THE_LEVIATHAN("The Leviathan", 95),
    THE_ROYAL_TITANS("The Royal Titans", 96),
    THE_WHISPERER("The Whisperer", 97),
    THEATRE_OF_BLOOD("Theatre of Blood", 98),
    THEATRE_OF_BLOOD_HM("Theatre of Blood: HM", 99),
    THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear Smoke Devil", 100),
    TOMBS_OF_AMASCUT("Tombs of Amascut", 101),
    TOMBS_OF_AMASCUT_EXPERT("Tombs of Amascut: Expert", 102),
    TZKAL_ZUK("TzKal-Zuk", 103),
    TZTOK_JAD("TzTok-Jad", 104),
    VARDORVIS("Vardorvis", 105),
    VENENATIS("Venenatis", 106),
    VET_ION("Vet'ion", 107),
    VORKATH("Vorkath", 108),
    WINTERTODT("Wintertodt", 109),
    YAMA("Yama", 110),
    ZALCANO("Zalcano", 111),
    ZULRAH("Zulrah", 112);

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

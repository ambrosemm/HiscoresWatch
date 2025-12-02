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
    // Skills - Added isSkill flag
    OVERALL("Overall", 0, true),
    ATTACK("Attack", 1, true),
    DEFENCE("Defence", 2, true),
    STRENGTH("Strength", 3, true),
    HITPOINTS("Hitpoints", 4, true),
    RANGED("Ranged", 5, true),
    PRAYER("Prayer", 6, true),
    MAGIC("Magic", 7, true),
    COOKING("Cooking", 8, true),
    WOODCUTTING("Woodcutting", 9, true),
    FLETCHING("Fletching", 10, true),
    FISHING("Fishing", 11, true),
    FIREMAKING("Firemaking", 12, true),
    CRAFTING("Crafting", 13, true),
    SMITHING("Smithing", 14, true),
    MINING("Mining", 15, true),
    HERBLORE("Herblore", 16, true),
    AGILITY("Agility", 17, true),
    THIEVING("Thieving", 18, true),
    SLAYER("Slayer", 19, true),
    FARMING("Farming", 20, true),
    RUNECRAFT("Runecraft", 21, true),
    HUNTER("Hunter", 22, true),
    CONSTRUCTION("Construction", 23, true),
    SAILING("Sailing",24, true),

    // Minigames (Clue Scrolls & Other) - isSkill is false
    GRID_POINTS("Grid Points", 25, false),
    LEAGUE_POINTS("League Points", 26, false),
    DEADMAN_POINTS("Deadman Points", 27, false),
    BOUNTY_HUNTER_HUNTER("Bounty Hunter - Hunter", 28, false),
    BOUNTY_HUNTER_ROGUE("Bounty Hunter - Rogue", 29, false),
    BOUNTY_HUNTER_HUNTER_LEGACY("Bounty Hunter (Legacy) - Hunter", 30, false),
    BOUNTY_HUNTER_ROGUE_LEGACY("Bounty Hunter (Legacy) - Rogue", 31, false),
    CLUE_SCROLL_ALL("Clue Scrolls (All)", 32, false),
    CLUE_SCROLL_BEGINNER("Clue Scrolls (Beginner)", 33, false),
    CLUE_SCROLL_EASY("Clue Scrolls (Easy)", 34, false),
    CLUE_SCROLL_MEDIUM("Clue Scrolls (Medium)", 35, false),
    CLUE_SCROLL_HARD("Clue Scrolls (Hard)", 36, false),
    CLUE_SCROLL_ELITE("Clue Scrolls (Elite)", 37, false),
    CLUE_SCROLL_MASTER("Clue Scrolls (Master)", 38, false),
    LMS_RANK("LMS - Rank", 39, false),
    PVP_ARENA_RANK("PvP Arena - Rank", 40, false),
    SOUL_WARS_ZEAL("Soul Wars Zeal", 41, false),
    RIFTS_CLOSED("Rifts Closed", 42, false),
    COLOSSEUM_GLORY("Colosseum Glory", 43, false),
    COLLECTIONS_LOGGED("Collections Logged", 44, false),

    // Bosses - isSkill is false
    ABYSSAL_SIRE("Abyssal Sire", 45, false),
    ALCHEMICAL_HYDRA("Alchemical Hydra", 46, false),
    AMOXLIATL("Amoxliatl", 47, false),
    ARAXXOR("Araxxor", 48, false),
    ARTIO("Artio", 49, false),
    BARROWS_CHESTS("Barrows Chests", 50, false),
    BRYOPHYTA("Bryophyta", 51, false),
    CALLISTO("Callisto", 52, false),
    CAL_VARION("Cal'varion", 53, false),
    CERBERUS("Cerberus", 54, false),
    CHAMBERS_OF_XERIC("Chambers of Xeric", 55, false),
    CHAMBERS_OF_XERIC_CM("Chambers of Xeric: CM", 56, false),
    CHAOS_ELEMENTAL("Chaos Elemental", 57, false),
    CHAOS_FANATIC("Chaos Fanatic", 58, false),
    COMMANDER_ZILYANA("Commander Zilyana", 59, false),
    CORPOREAL_BEAST("Corporeal Beast", 60, false),
    CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", 61, false),
    DAGANNOTH_PRIME("Dagannoth Prime", 62, false),
    DAGANNOTH_REX("Dagannoth Rex", 63, false),
    DAGANNOTH_SUPREME("Dagannoth Supreme", 64, false),
    DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", 65, false),
    DOOM_OF_MOKHAIOTL("Doom of Mokhaiotl", 66, false),
    DUKE_SUCELLUS("Duke Sucellus", 67, false),
    GENERAL_GRAARDOR("General Graardor", 68, false),
    GIANT_MOLE("Giant Mole", 69, false),
    GROTESQUE_GUARDIANS("Grotesque Guardians", 70, false),
    HESPORI("Hespori", 71, false),
    KALPHITE_QUEEN("Kalphite Queen", 72, false),
    KING_BLACK_DRAGON("King Black Dragon", 73, false),
    KRAKEN("Kraken", 74, false),
    KREE_ARRA("Kree'arra", 75, false),
    K_RIL_TSUTSAROTH("K'ril Tsutsaroth", 76, false),
    LUNAR_CHESTS("Lunar Chests", 77, false),
    MIMIC("Mimic", 78, false),
    NEX("Nex", 79, false),
    NIGHTMARE("The Nightmare", 80, false),
    PHOSANIS_NIGHTMARE("Phosani's Nightmare", 81, false),
    OBOR("Obor", 82, false),
    PHANTOM_MUSPAH("Phantom Muspah", 83, false),
    SARACHNIS("Sarachnis", 84, false),
    SCORPIA("Scorpia", 85, false),
    SCURRIUS("Scurrius", 86, false),
    SHELLBANE_GRYPHON("Shellbane Gryphon", 87, false),
    SKOTIZO("Skotizo", 88, false),
    SOL_HEREDIT("Sol Heredit", 89, false),
    SPINDEL("Spindel", 90, false),
    TEMPOROSS("Tempoross", 91, false),
    THE_GAUNTLET("The Gauntlet", 92, false),
    THE_CORRUPTED_GAUNTLET("The Corrupted Gauntlet", 93, false),
    THE_HUEYCOATL("The Hueycoatl", 94, false),
    THE_LEVIATHAN("The Leviathan", 95, false),
    THE_ROYAL_TITANS("The Royal Titans", 96, false),
    THE_WHISPERER("The Whisperer", 97, false),
    THEATRE_OF_BLOOD("Theatre of Blood", 98, false),
    THEATRE_OF_BLOOD_HM("Theatre of Blood: HM", 99, false),
    THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear Smoke Devil", 100, false),
    TOMBS_OF_AMASCUT("Tombs of Amascut", 101, false),
    TOMBS_OF_AMASCUT_EXPERT("Tombs of Amascut: Expert", 102, false),
    TZKAL_ZUK("TzKal-Zuk", 103, false),
    TZTOK_JAD("TzTok-Jad", 104, false),
    VARDORVIS("Vardorvis", 105, false),
    VENENATIS("Venenatis", 106, false),
    VET_ION("Vet'ion", 107, false),
    VORKATH("Vorkath", 108, false),
    WINTERTODT("Wintertodt", 109, false),
    YAMA("Yama", 110, false),
    ZALCANO("Zalcano", 111, false),
    ZULRAH("Zulrah", 112, false);

    private final String name;
    private final int apiIndex;
    private final boolean isSkill;

    @Override
    public String toString()
    {
        return name;
    }
}

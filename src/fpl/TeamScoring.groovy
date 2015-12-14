package fpl

import groovy.json.JsonOutput

/**
 * Created by dlewis-crosby on 11/12/2015.
 */

leagueUrl = "http://fantasy.premierleague.com/entry/1234966/event-history/16/"
fplBaseUrl = "http://fantasy.premierleague.com/fixture"
currentGame = 0
currentWeek = 0

gameHtml = getNextGame()

teamNames = ['Man City','Arsenal','Man Utd','Spurs','Leicester','Everton','Southampton','West Ham','Watford','Stoke','Liverpool','Crystal Palace',
        'West Brom','Chelsea','Newcastle','Sunderland','Norwich','Bournemouth','Swansea','Aston Villa']

teams = []
setupTeams(teams)

while (hasGameBeenPlayed(gameHtml)) {

    def teamGameTeamNames = getTeams (gameHtml)
    def scores = getTeamScores (gameHtml)
    //println "${teamNames.homeTeam} (${scores.homeTeam}) vs ${teamNames.awayTeam} (${scores.awayTeam})"

    addHomeTeamScore(teamGameTeamNames.homeTeam, scores.homeTeam)
    addAwayTeamScore(teamGameTeamNames.awayTeam, scores.awayTeam)

    gameHtml = getNextGame()
}

teams.each { team ->
    println team
}
addTotals (teams)

def homeCols = ["pos", "team.............", "leaguePos", "homeMatches", "homePts", "avgHomePts"]
def awayCols = ["pos", "team.............", "leaguePos", "awayMatches", "awayPts", "avgAwayPts"]
def allCols = ["pos", "team.............", "leaguePos", "homeMatches", "homePts", "avgHomePts", "awayMatches", "awayPts", "avgAwayPts", "totalPts", "avgPts"]
outputResults (teams, homeCols, "homePts")
outputResults (teams, awayCols, "awayPts")
outputResults (teams, allCols, "totalPts")


def setupTeams(ArrayList teams) {
    def leagueTableHtml = saveLeagueHtml()
    teamNames.each { teamName ->
        def leaguePos = getLeaguePos (teamName, leagueTableHtml)
        teams << [name: teamName, homeGamePoints: [], awayGamePoints: [], leaguePos: leaguePos]
    }
}

def outputResults (teams, cols, sortCol) {

    println "\n\n${cols.join(" ")}"

    teams = teams.sort {it[sortCol]}.reverse()

    teams.eachWithIndex { team, i ->
        def rowData = []
        cols.each { col ->
            def data
            if (col == "pos") {
                rowData << pad (i+1, 3)
            }
            else if (col == "team.............") {
                rowData << padTeam(team.name)
            }
            else {
                rowData << pad (team[col], col.length())
            }
        }
        println "${rowData.join(" ")}"
    }
}

def addTotals (ArrayList teams) {
    teams.each { team->
        team.homeMatches = team.homeGamePoints.size()
        team.awayMatches = team.awayGamePoints.size()
        team.homePts = team.homeGamePoints.sum()
        team.awayPts = team.awayGamePoints.sum()
        team.avgHomePts = Math.round(team.homePts/team.homeMatches)
        team.avgAwayPts = Math.round(team.awayPts/team.awayMatches)
        team.totalPts = team.homePts + team.awayPts
        team.avgPts = Math.round(team.totalPts / (team.homeMatches + team.awayMatches))
    }
}

def padTeam (String team) {
    while (team.length() < 17) {
        team = team + '.'
    }
    team
}

def padPos (pos, actualPos) {
    pos = pos < 10 ? "..$pos" : ".$pos"
    actualPos = actualPos < 10 ? "........$actualPos" : ".......$actualPos"
    "${pos} ${actualPos}"
}

def pad (text, places) {
    text = "$text"
    while (text.length() < places) {
        text = '.' + text
    }
    text
}

def saveLeagueHtml () {
    def leagueHtml = new URL(leagueUrl).text

    def leagueTableRegex = /(?s)<table [^>]+class="leagueTable">((?:(?!<\/table>).)+)<\/table>/
    def leagueTableMatcher = (leagueHtml =~ leagueTableRegex)
    def leagueTable = leagueTableMatcher[0][1]

    def leagueFile = new File ("league.html")
    leagueFile.write leagueTable as String
    leagueTable
}

def getLeaguePos (String team, String leagueTableHtml) {

    def teamRegex = /(?s)<td class="col-club"><a[^>]+>\s+((?:(?!\s+<\/a>).)+)\s+<\/a>/
    def teamMatcher = (leagueTableHtml =~ teamRegex)

    teamMatcher.findIndexOf { entry ->
        entry[1] == team
    } + 1
}

def addLeaguePositions() {

    def positionHtml = new URL(leagueUrl).text

    def leagueTableRegex = /(?s)<table [^>]+class="leagueTable">((?:(?!<\/table>).)+)<\/table>/

    def leagueTableMatcher = (positionHtml =~ leagueTableRegex)
    def leagueTable = leagueTableMatcher[0][1]

    def teamRegex = /(?s)<td class="col-club"><a[^>]+>\s+((?:(?!\s+<\/a>).)+)\s+<\/a>/
    def teamMatcher = (leagueTable =~ teamRegex)

    teamMatcher.eachWithIndex { entry, i ->
        def teamObj = teams.find {entry[1] == it.name}
        teamObj?.actualPos = (i + 1)
    }
}

def addHomeTeamScore (teamName, score) {
    def team = teams.find { it.name == teamName}
    team.homeGamePoints << score
}

def addAwayTeamScore (teamName, score) {
    def team = teams.find { it.name == teamName}
    team.awayGamePoints << score
}

def updateGameweek() {
    def week = 1 + ((currentGame / 10) as int)
    if (week != currentWeek) {
        currentWeek++
    }
}

def getNextGame() {
    updateGameweek()
    currentGame ++
    getGameHtml(currentGame, currentWeek)
}

def getTeams (String gameHtml) {
    def regex = /(?s)<caption>((?:(?!<\/caption>).)+)<\/caption>/
    def matcher = (gameHtml =~ regex)

    def homeTeam = matcher[0][1]
    def awayTeam = matcher[1][1]

    [homeTeam: homeTeam, awayTeam: awayTeam]
}

def getTeamScores (String gameHtml) {
    def teamBodyRegex = /(?s)<tbody>((?:(?!<\/tbody>).)+)<\/tbody>/
    def bodyMatcher = (gameHtml =~ teamBodyRegex)
    def homeTeamBody = bodyMatcher[0][1]
    def awayTeamBody = bodyMatcher[1][1]

    def homeTeamScores = getPlayerScores(homeTeamBody)
    def awayTeamScores = getPlayerScores(awayTeamBody)

    [homeTeam: homeTeamScores.sum(), awayTeam: awayTeamScores.sum()]
}

def getPlayerScores (String teamHtml) {

    def playerScoreRegex = /(?s)<td>\s+([0-9]+)\s+<\/td>\s+<\/tr>/
    (teamHtml =~ playerScoreRegex).collect { all, score ->
        score as int
    }
}

def hasGameBeenPlayed(String gameHtml) {
    def regex = /<table/
    gameHtml =~ regex
    //currentGame < 12
}

def getGameHtml (int gameNumber, gameWeek) {

    def gameDir = new File("games/$gameWeek")
    gameDir.mkdirs()
    def gameFile = new File (gameDir, "$currentGame")
    if (!gameFile.exists() || !hasGameBeenPlayed(gameFile.text)) {
        def url = "$fplBaseUrl/$gameNumber/"
        gameFile.write new URL(url).text
    }
    gameFile.text
}
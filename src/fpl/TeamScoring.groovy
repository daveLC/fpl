package fpl

/**
 * Created by dlewis-crosby on 11/12/2015.
 */


fplBaseUrl = "http://fantasy.premierleague.com/fixture"
currentGame = 0
currentWeek = 0

gameHtml = getNextGame()

while (hasGameBeenPlayed(gameHtml)) {

    def teams = getTeams (gameHtml)
    def scores = getTeamScores (gameHtml)
    println "${teams.homeTeam} (${scores.homeTeam}) vs ${teams.awayTeam} (${scores.awayTeam})"

    gameHtml = getNextGame()
}

def updateGameweek() {
    def week = 1 + ((currentGame / 10) as int)
    if (week != currentWeek) {
        currentWeek++
        println "\nGameweek $currentWeek\n-----------"
    }
}

def getNextGame() {
    updateGameweek()
    currentGame ++
    getGameHtml(currentGame)
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
    currentGame < 12
}

def getGameHtml (int gameNumber) {
    def url = "$fplBaseUrl/$gameNumber/"
    new URL(url).text
}
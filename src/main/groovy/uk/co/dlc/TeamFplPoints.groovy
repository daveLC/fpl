package uk.co.dlc

import groovy.json.JsonSlurper

// General data
def globalData = getGlobalData();

// Get the data for each GW
def eventUrlTemplate = "https://fantasy.premierleague.com/drf/event/GW/live"

teamData = []

(23..30).each {

    def eventUrl = eventUrlTemplate.replace("GW", it as String)

    def eventData = getJsonData(eventUrl)

    // Loop through the fixtures
    eventData.fixtures.eachWithIndex { fixtureData, i ->

        // Get the teams
        def homeTeam = globalData.teams.find{ it.id == fixtureData.team_h }
        def awayTeam = globalData.teams.find{ it.id == fixtureData.team_a }

        println "Match ${i+1}: ${homeTeam.name} v ${awayTeam.name}"

        // Get players from the stats.bps list
        def homePlayerIds = fixtureData.stats.bps.h.element[0]
        def awayPlayerIds = fixtureData.stats.bps.a.element[0]
        def homePlayers = getPlayerData(homePlayerIds, eventData.elements, globalData.players)
        def awayPlayers = getPlayerData(awayPlayerIds, eventData.elements, globalData.players)

        def homeTeamValues = calculateAllValues(homePlayers)
        //def homeTeamValues = calculateTop10Values(homePlayers)
        def awayTeamValues = calculateAllValues(awayPlayers)
        //def awayTeamValues = calculateTop10Values(awayPlayers)

        println "  Home players:"
        println "    pts: ${homeTeamValues.points}"
        println "    avg: ${homeTeamValues.average}"
        println "  Away players:"
        println "    pts: ${awayTeamValues.points}"
        println "    avg: ${awayTeamValues.average}"
        println "---"

        def homeTeamOverall = getTeamData(homeTeam)
        def awayTeamOverall = getTeamData(awayTeam)

        updateValues(homeTeamOverall, homeTeamValues)
        updateValues(awayTeamOverall, awayTeamValues)
    }
}

teamData.each { team ->
    team.home.avg = calculateAvg(team.home.pts, team.home.players)
    team.away.avg = calculateAvg(team.away.pts, team.away.players)
    team.total.avg = calculateAvg(team.total.pts, team.total.players)
    team.gk.avg = calculateAvg(team.gk.pts, team.gk.players)
    team.df.avg = calculateAvg(team.df.pts, team.df.players)
    team.md.avg = calculateAvg(team.md.pts, team.md.players)
    team.at.avg = calculateAvg(team.at.pts, team.at.players)
}
println "pos team             pts   gk   df   md   at"
teamData.sort{it.total.pts}.reverse().eachWithIndex { team, i ->
    println "${padNumber2(i+1)}. ${padRight(team.name)} ${padNumber4(team.total.pts)}  ${padNumber3(team.gk.pts)}  ${padNumber3(team.df.pts)}  ${padNumber3(team.md.pts)}  ${padNumber3(team.at.pts)}"
}
teamData.sort{it.total.pts}.reverse().eachWithIndex { team, i ->
    println "${i+1}. ${team.name} - ${team.total.avg}, ${team.total.pts} - ${team.home.avg}, ${team.home.pts} - ${team.away.avg}, ${team.away.pts}"
}

def padNumber2(value) {
    String.format("%2d", value);
}
def padNumber3(value) {
    String.format("%3d", value);
}
def padNumber4(value) {
    String.format("%4d", value);
}

def padRight(String value, length = 15) {
    def padding = ' '.multiply(length - value.size())
    "${value}${padding}"
}

def updateValues(team, values) {
    team.home.pts += values.points
    team.total.pts += values.points
    team.home.players += values.players
    team.total.players += values.players
    team.gk.pts += values.positions.gk.points
    team.gk.players += values.positions.gk.players
    team.df.pts += values.positions.df.points
    team.df.players += values.positions.df.players
    team.md.pts += values.positions.md.points
    team.md.players += values.positions.md.players
    team.at.pts += values.positions.at.points
    team.at.players += values.positions.at.players
}

def calculateAllValues(players) {
    def points = players.sum {it.points}
    def playerNumber = players.size()
    [
            points: points,
            players: playerNumber,
            average: points / playerNumber,
            positions: addPositionValues(players)
    ]
}

def calculateTop10Values(players) {
    def top10Players = players.take(10)
    def points = top10Players.sum {it.points}
    def playerNumber = 10
    [
            points: points,
            players: playerNumber,
            average: points / playerNumber,
            positions: addPositionValues(top10Players)
    ]
}

def addPositionValues(players) {
    def gks = players.findAll{it.position == "gk"}
    def dfs = players.findAll{it.position == "df"}
    def mds = players.findAll{it.position == "md"}
    def ats = players.findAll{it.position == "at"}
    [
            gk: [
                    points: gks.size() ? gks.sum{it.points} : 0,
                    players: gks.size()
            ],
            df: [
                    points: dfs.size() ? dfs.sum{it.points} : 0,
                    players: dfs.size()
            ],
            md: [
                    points: mds.size() ? mds.sum{it.points} : 0,
                    players: mds.size()
            ],
            at: [
                    points: ats.size() ? ats.sum{it.points} : 0,
                    players: ats.size()
            ]
    ]
}


def calculateAvg(points, players) {
    def avg = "0.00"
    if (points && players) {
        avg = Math.round((points / players) * 100.00) / 100.00
        avg = String.format( "%.2f", avg )
    }
    avg
}

def getPlayerData(ids, elements, players) {
    ids.collect { playerId ->
        def element = elements.get(playerId as String)
        def player = players.find {it.id == playerId}
        [
                id: playerId,
                name: player.name,
                points: element.stats.total_points,
                position: player.position
        ]
    }.sort {it.points}.reverse()
}

def getTeamData (team) {
    def data = teamData.find {it.id == team.id}
    if (!data) {

        data = [
                id   : team.id,
                name : team.name,
                home : [
                        pts    : 0,
                        players: 0
                ],
                away : [
                        pts    : 0,
                        players: 0
                ],
                total: [
                        pts    : 0,
                        players: 0
                ],
                gk: [
                        pts    : 0,
                        players: 0
                ],
                df: [
                        pts    : 0,
                        players: 0
                ],
                md: [
                        pts    : 0,
                        players: 0
                ],
                at: [
                        pts    : 0,
                        players: 0
                ],
        ]
        teamData.add(data)
    }
    data
}

def getGlobalData() {

    def dataUrl = "https://fantasy.premierleague.com/drf/bootstrap-static"
    def globalData = getJsonData(dataUrl)
    def teams = globalData.teams.collect { team ->
        [
                id: team.id,
                name: team.name
        ]
    }
    def players = globalData.elements.collect { player ->
        [
                id: player.id,
                name: player.web_name,
                position: getPlayerPosition(player.element_type)
        ]
    }
    [
            teams: teams,
            players: players
    ]
}

def getPlayerPosition(type) {
    type == 1 ? "gk" : type == 2 ? "df" : type == 3 ? "md" : type == 4 ? "at" : ""
}

def getJsonData(String url) {
    new JsonSlurper().parseText(new URL(url).text)
}
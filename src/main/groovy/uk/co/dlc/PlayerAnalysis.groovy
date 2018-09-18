package uk.co.dlc

import groovy.json.JsonSlurper

leaugeBaseUrl = "https://fantasy.premierleague.com/drf/leagues-classic-standings/313?ls-page="
entryBaseUrl = "https://fantasy.premierleague.com/drf/entry"
teamDataUrl = "https://fantasy.premierleague.com/drf/bootstrap-static"

teamsToCheck = ['BOU','WBA','HUD','CRY','STK','EVE','LIV','WAT']
gameweek = "30"
playersToIgnore = []
milestonesToCheck = [1,10,100,1000,10000]

playersInEntries()

def playersInEntries() {
    // Get players from the teams
    def teamPlayers = getTeamPlayers(teamsToCheck)

    // Get the entryIds
    def entryIds = getEntryIdsForNEntries(milestonesToCheck.max() + 1)
    // Find out how many players on average
    checkEntriesForPlayers(entryIds, teamPlayers)

    teamPlayers.findAll{it.count > 1}.sort{it.count}.reverse().collect { println "${it.name}: ${it.count}" }
}

def checkEntriesForPlayers(entryIds, teamPlayers) {
    def entries = []
    entryIds.eachWithIndex { entry, i ->
        if (i % 50 == 0) {
            println "${new Date()} $i"
        }
        entries << [id: entry, players: getPlayersInTeam(entry, teamPlayers)]

        if (milestonesToCheck.contains(i+1)) {
            println "first ${entries.size()} players, average for GW${gameweek}: ${entries.sum { it.players } / entries.size()} players"
        }
    }
}

def getEntryIdsForNEntries(entryNumber){
    def entryIds = []
    def page = 1
    def retrieved = 0
    while (entryNumber > retrieved){
        entryIds += getEntryIds(page, entryNumber - retrieved > 50 ? 50 : entryNumber - retrieved)
        retrieved += 50
        page++
    }
    entryIds
}

def getPlayersInTeam (entryId, teamPlayers) {
    def players = 0
    def teamPlayerIds = teamPlayers*.id
    def entryUrl = entryBaseUrl+ "/$entryId/event/$gameweek/picks"
    def text = new URL(entryUrl).text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    json.picks.each { pick ->
        if (teamPlayerIds.contains(pick.element)) {
            players++
            def player = teamPlayers.find{it.id == pick.element}
            player.count++
        }
    }
    players
}

def getEntryIds (page, count) {
    def playerIds = []
    def text = new URL("$leaugeBaseUrl$page").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    (0..count-1).each {
        playerIds << json.standings.results[it].entry
    }
    playerIds
}

def getTeamPlayers(shortNames) {
    def teamPlayers = []
    def teamIds = []
    def text = new URL(teamDataUrl).text
    def json = new JsonSlurper().parseText(text)
    json.teams.each {
        if (shortNames.contains(it.short_name)) {
            teamIds << it.code
        }
    }
    json.elements.each {
        if (teamIds.contains(it.team_code) && !isBadPlayer(it.web_name)) {
            teamPlayers << [id: it.id, name: it.web_name, count:0]
        }
    }
    teamPlayers
}

def isBadPlayer(name) {
    playersToIgnore.contains(name)
}

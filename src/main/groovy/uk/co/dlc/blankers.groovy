package uk.co.dlc

import groovy.json.JsonSlurper

/**
 * Created by dlc on 16/10/2016.
 */

// 50 per page
url = "https://fantasy.premierleague.com/drf/leagues-classic-standings/313?ls-page="


// Get all player ids from the 6 teams playing
// teams
// https://fantasy.premierleague.com/drf/bootstrap-static

gw = "37"

// get team ids
def teamNames = ['TOT', 'SOU', 'LEI', 'MUN']
def teamPlayerIds = getTeamPlayerIds(teamNames)


// Get the first n player ids
def entryIds = getEntryIdsForNEntries(10000)
def milestones = [10,100,1000, 5000]
// and see how many players they have
def entries = []
entryIds.eachWithIndex { entry, i ->
    if (i % 500 == 0) {
        println "${new Date()} $i"
    }
    entries << [id: entry, players: getPlayersInTeam(entry, teamPlayerIds)]
    if (milestones.contains(i)) {
        println "first ${i} players, average for GW$gw: ${entries.sum { it.players } / i} players"
    }
}
println "first ${entries.size()} players, average for GW$gw: ${entries.sum {it.players}/entries.size()} players"

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

def getPlayersInTeam (entryId, teamPlayerIds) {
    def players = 0
    def text = new URL("https://fantasy.premierleague.com/drf/entry/$entryId/event/$gw/picks").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    json.picks.each {
        if (teamPlayerIds.contains(it.element)) {
            players++
        }
    }
    players
}

def getEntryIds (page, count) {
    def playerIds = []
    def text = new URL("$url$page").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    (0..count-1).each {
        playerIds << json.standings.results[it].entry
    }
    playerIds
}

def getTeamPlayerIds(shortNames) {
    def teamPlayerIds = []
    def teamIds = []
    def text = new URL("https://fantasy.premierleague.com/drf/bootstrap-static").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    json.teams.each {
        if (shortNames.contains(it.short_name)) {
            teamIds << it.code
        }
    }
    json.elements.each {
        if (teamIds.contains(it.team_code) && isNotBadPlayer(it.web_name)) {
            teamPlayerIds << it.id
        }
    }
    teamPlayerIds
}

def isNotBadPlayer(name) {
    ['Amat','Stanislas','Holgate','Mings','Francis','Bolasie','Naughton','Dawson']
}

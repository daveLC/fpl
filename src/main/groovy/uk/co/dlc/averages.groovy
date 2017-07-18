package uk.co.dlc

import groovy.json.JsonSlurper

// 50 per page
rankingUrl = "https://fantasy.premierleague.com/drf/leagues-classic-standings/313?ls-page="
entryUrl = "https://fantasy.premierleague.com/drf/entry/"


// Get the first n player ids
def entryIds = getEntryIdsForNEntries(10000)
def milestones = [10,100,1000,5000]

def entries = []
entryIds.eachWithIndex { entry, i ->

    // get their gw score
    def gameweekScore = getEntryScore(entry)

    if (milestones.contains(i)) {
        println "top ${i} average: ${entries.sum() / i}pts"
    }
    entries << gameweekScore
}
println "top ${entries.size()} average: ${entries.sum() / entries.size()}pts"

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

def getEntryScore (entryId) {

    def text = new URL("$entryUrl$entryId").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    def gameweekScore = json.entry.summary_event_points
    gameweekScore
}

def getEntryIds (page, count) {
    def playerIds = []
    def text = new URL("$rankingUrl$page").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    (0..count-1).each {
        playerIds << json.standings.results[it].entry
    }
    playerIds
}


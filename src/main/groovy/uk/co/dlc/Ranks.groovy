package uk.co.dlc

import groovy.json.JsonSlurper

import java.text.DecimalFormat

url = "https://fantasy.premierleague.com/drf/leagues-classic-standings/313?ls-page="

myId = 121943
itemsPerPage = 50

def rankings = [1,10,100,1000,2000,3000,4000,5000,10000,15000,20000,30000,40000,50000,100000,200000,300000,400000,500000,1000000,2000000,3000000,4000000,5000000]

def pages = rankings.collect { it / itemsPerPage > 1 ? it /itemsPerPage : 1}
def offsets = rankings.collect { (it % itemsPerPage) - 1}

def dateText = new Date().format("yy-MM-dd")
def file = new File("ranks/ranks-${dateText}.txt")

pages.eachWithIndex { page, i ->
    def rank = rankings[i]
    def points = getPoints(page, offsets[i])
    def line = getLine(rank, points)
    println line
    file << "\n$line"
}

def getLine(rank, points) {
    def formattedRank = formatRank(rank)
    "${formattedRank}: ${points}"
}

def formatRank(rank) {
    def pattern = "###,###,###"
    def numberFormat = new DecimalFormat(pattern)
    numberFormat.format(rank)
}

def myInfo = getMyInfo()

def line = getLine(myInfo.rank, myInfo.points) + " (Me)"
println line
file << "\n$line"
file << "\n---------------------------\n"

def getMyInfo() {
    def text = new URL("https://fantasy.premierleague.com/drf/entry/$myId/history").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    [points: json.entry.summary_overall_points, rank: json.entry.summary_overall_rank]
}

def getPoints (page, offset) {
    def text = new URL("$url$page").text
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText(text)
    json.standings.results[offset].total
}
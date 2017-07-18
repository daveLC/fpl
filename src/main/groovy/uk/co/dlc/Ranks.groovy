package uk.co.dlc

import groovy.json.JsonSlurper

url = "https://fantasy.premierleague.com/drf/leagues-classic-standings/313?ls-page="

// 50 per page

def rankings = [1,10,100,1000,5000,10000,50000,100000,200000,300000,400000,500000,1000000]
def pages = rankings.collect { it / 50 > 1 ? it /50 : 1}
def offsets = rankings.collect { (it % 50) - 1}

def dateText = new Date().format("yy-MM-dd")
def file = new File("ranks/ranks-${dateText}.txt")
println file.absolutePath
pages.eachWithIndex { page, i ->
    def line = "${getPoints(page, offsets[i])} (${rankings[i]})"
    println line
    file << "\n$line"
}

def myInfo = getMyInfo()

def line = "Me: ${myInfo.points} (${myInfo.rank})"
println line
file << "\n$line"
file << "\n---------------------------\n"

def getMyInfo() {
    def text = new URL("https://fantasy.premierleague.com/drf/entry/234408").text
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
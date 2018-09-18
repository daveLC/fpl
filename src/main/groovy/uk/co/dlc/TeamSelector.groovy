/**
 * Created by Dave on 06/08/2015.
 */
package uk.co.dlc

playerFileBase = new File("C:/Users/dlc/dev/fpl/src/main/resources")
budget = 100

keeperOptions = []
defenderOptions = []
midOptions = []
forwardOptions = []

keeperQty = 2
defenderQty = 5
midQty = 5
forwardQty = 3

create()

def create() {
    buildOptions()
    def team = [
            keepers: createPlayerList([], keeperOptions),
            defenders: createPlayerList(['trippier'], defenderOptions),
            mids: createPlayerList(['willian'], midOptions),
            forwards: createPlayerList(['lukaku', 'kane'], forwardOptions)
    ]
    createTeams(30, team)
}

def createPlayerList (List<String> players, options) {
    def list = []
    players.each { player ->
        def p = options.find {it.name == player}
        if (p) {
            list << p
        }
    }
    list as HashSet
}

def createTeams (int n) {
    def team = [
            keepers: [],
            defenders: [],
            mids: [],
            forwards: []
    ]
    createTeams(n, team)
}

def createTeams (int numberOfTeams, Map originalSeed) {
    def teams = []
    while (teams.size() < numberOfTeams) {
        def team = deepcopy (originalSeed)
        populateTeam(team)
        if (teamIsValid(team)) {
            teams.add team
        }
        else {
            //outputTeam(team)
        }
        println "teams so far: ${teams.size()}"
    }
    println "\n============================== FINAL TEAMS =================================="
    teams.each {
        outputTeam(it)
    }
}

def deepcopy(orig) {
    bos = new ByteArrayOutputStream()
    oos = new ObjectOutputStream(bos)
    oos.writeObject(orig); oos.flush()
    bin = new ByteArrayInputStream(bos.toByteArray())
    ois = new ObjectInputStream(bin)
    return ois.readObject()
}

def teamIsValid (team) {
    boolean valid = true

    if (getTotalValue(team) > budget) {
        //println "too expensive: ${getTotalValue(team)}"
        valid = false
    }
    teams = []
    teams << team.keepers*.team
    teams << team.defenders*.team
    teams << team.mids*.team
    teams << team.forwards*.team
    teams = teams.flatten()

    def uniqueTeams = teams.unique(false)
    uniqueTeams.each { t ->
        if (teams.count {it == t} > 3) {
            //println "too many $t"
            valid = false
        }
    }
    valid
}

def outputTeam(team) {
    println "           ${team.keepers[0].name}  ${team.keepers[1].name}  "
    println "  ${team.defenders[0].name}  ${team.defenders[1].name}  ${team.defenders[2].name}  ${team.defenders[3].name}  ${team.defenders[4].name}  "
    println "  ${team.mids[0].name}  ${team.mids[1].name}  ${team.mids[2].name}  ${team.mids[3].name}  ${team.mids[4].name}  "
    println "         ${team.forwards[0].name}  ${team.forwards[1].name}  ${team.forwards[2].name}"
    println " total value: ${getTotalValue(team)}"
    println ""
}

def getTotalValue (team) {
    team.keepers.sum { it.value } + team.defenders.sum { it.value} + team.mids.sum { it.value} + team.forwards.sum { it.value}
}

def populateTeam (team) {

    team.keepers = createPosition (keeperOptions, keeperQty, team.keepers as HashSet)
    team.defenders = createPosition (defenderOptions, defenderQty, team.defenders as HashSet)
    team.mids = createPosition (midOptions, midQty, team.mids as HashSet)
    team.forwards = createPosition (forwardOptions, forwardQty, team.forwards as HashSet)
}

def createPosition (ArrayList array, int qty, HashSet players = new HashSet()) {

    while (players.size() < qty) {
        def player = chooseRandomPlayer(array)
        players.add (player)
    }
    players
}

def chooseRandomPlayer(ArrayList array) {
    int rnd = new Random().nextInt(array.size())
    array[rnd]
}

def buildOptions() {

    importFromCsv(new File(playerFileBase, "gks.csv"), keeperOptions)
    importFromCsv(new File(playerFileBase, "def.csv"), defenderOptions)
    importFromCsv(new File(playerFileBase, "mid.csv"), midOptions)
    importFromCsv(new File(playerFileBase, "for.csv"), forwardOptions)
}

def importFromCsv(File csvFile, optionList) {

    csvFile.eachLine { line ->
        def values = line.split(",")
        optionList << [name:values[0], team: values[1], value:values[2] as float]
    }
}

def buildKeeperOptions() {
    keeperOptions << [name: 'Panti', value: 5, team: 'SUN']
    keeperOptions << [name: 'Ruddy', value: 4.5, team: 'NOR']
    keeperOptions << [name: 'Hennessey', value: 4, team: 'CRY']
    keeperOptions << [name: 'Schmeich', value: 4.5, team: 'LEI']
    keeperOptions << [name: 'Butland', value: 4.5, team: 'STO']
}

def buildDefenderOptions() {
    defenderOptions << [name: 'Ivan', value: 7, team: 'CHE']
    defenderOptions << [name: 'Mert', value: 5.5, team: 'ARS']
    defenderOptions << [name: 'Azpi', value: 6, team: 'CHE']
    defenderOptions << [name: 'Kosc', value: 6, team: 'ARS']
    defenderOptions << [name: 'Cedric', value: 5, team: 'SOU']
    defenderOptions << [name: 'Francis', value: 4.5, team: 'BOU']
    defenderOptions << [name: 'Mings', value: 4.5, team: 'BOU']
    defenderOptions << [name: 'Bassong', value: 4.5, team: 'NOR']
    defenderOptions << [name: 'Jenks', value: 5, team: 'WHU']
    defenderOptions << [name: 'Huth', value: 4.5, team: 'LEI']
    defenderOptions << [name: 'Ward', value: 4.5, team: 'CRY']
    defenderOptions << [name: 'Kol', value: 5, team: 'MCI']
}

def buildMidOptions() {
    midOptions << [name: 'Fab', value: 9, team: 'CHE']
    midOptions << [name: 'Hazard', value: 11.5, team: 'CHE']
    midOptions << [name: 'Chadli', value: 7, team: 'TOT']
    midOptions << [name: 'Ramsey', value: 8.5, team: 'ARS']
    midOptions << [name: 'Wally', value: 9, team: 'ARS']
    midOptions << [name: 'Mane', value: 8, team: 'SOU']
    midOptions << [name: 'Ibe', value: 5, team: 'LIV']
    midOptions << [name: 'Mahrez', value: 5.5, team: 'LEI']
    midOptions << [name: 'Hendo', value: 7, team: 'LIV']
    midOptions << [name: 'Milner', value: 7, team: 'LIV']
    midOptions << [name: 'Cazorla', value: 8.5, team: 'ARS']
    midOptions << [name: 'Ozil', value: 8.5, team: 'ARS']
    midOptions << [name: 'Ritchie', value: 6, team: 'BOU']
    midOptions << [name: 'Mata', value: 8.5, team: 'MUN']
    midOptions << [name: 'Sterling', value: 9, team: 'MCI']
}

def buildForwardOptions() {
    forwardOptions << [name: 'Roo', value: 10.5, team: 'MUN']
    forwardOptions << [name: 'Tek', value: 8.5, team: 'LIV']
    forwardOptions << [name: 'Sakho', value: 6.5, team: 'WHU']
    forwardOptions << [name: 'Pelle', value: 8, team: 'SOU']
    forwardOptions << [name: 'Deeney', value: 5.5, team: 'WAT']
    forwardOptions << [name: 'Wilson', value: 5.5, team: 'BOU']
    forwardOptions << [name: 'Vardy', value: 6, team: 'LEI']
    forwardOptions << [name: 'Murray', value: 6, team: 'CRY']
}
import static Settings.*
import static ConsoleUI.*
import static java.lang.Math.*
import java.text.*

class Settings
{
    static final File HOME_DIR = new File(System.getProperty("user.home"))
    static final File CURRENT_DIR = new File(System.getProperty("user.dir"))

    /** The configuration file that is read at startup. */
    static final File CONFIG_FILE = new File(HOME_DIR, ".vokabeltrainer")
    static final Properties CONFIG_PROPS = new Properties()

    /* directory where all vocabulary files are stored */
    static File VOCS_DIR
    static File VOCS_FILE
    static final String CHARSET = "utf-8"
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

    static void initialize() {
        boolean isFirstRun = !CONFIG_FILE.exists()
        if (isFirstRun) {
            writeln("===== Configuring VokabelTrainer for the first time =====")
            writeln("Where should your vocabulary files be stored?")
            writeln("  1 : home directory     : ${HOME_DIR.getCanonicalPath()}")
            writeln("  2 : current directory  : ${CURRENT_DIR.getCanonicalPath()}")
            writeln("  for a custom directory : please enter the full path below")
            write   "Please enter your choice: "
            String answer = readAnswer()
            switch (answer) {
                case "": VOCS_DIR = HOME_DIR; break
                case "1": VOCS_DIR = HOME_DIR; break
                case "2": VOCS_DIR = CURRENT_DIR; break
                default:
                    def dir = new File(answer)
                    dir.mkdir()
                    if (dir.exists() && dir.isDirectory() && dir.canWrite()) {
                        VOCS_DIR = dir
                    } else {
                        println "Error: this directory is not writable"
                        VOCS_DIR = HOME_DIR
                    }
            }
            println "\nInfo: selected vocabulary directory: ${VOCS_DIR.getCanonicalPath()}"
            CONFIG_PROPS["VOCS_DIR"] = VOCS_DIR.getCanonicalPath()
            CONFIG_PROPS.store(CONFIG_FILE.newWriter(CHARSET), "VokabelTrainer Configuration")
            println "Info: saved configuration file: ${CONFIG_FILE.getCanonicalPath()}"
        } else {  // normal run
            CONFIG_PROPS.load(CONFIG_FILE.newReader(CHARSET))
            VOCS_DIR = new File(CONFIG_PROPS["VOCS_DIR"])
        }
        VOCS_FILE = new File(VOCS_DIR, "vocabulary.txt")
    }

    static void selectVocsFile() {
        if (VOCS_FILE.exists()) {
            List files = []
            VOCS_DIR.eachFileMatch (~/vocabulary*.*.txt/) { files.add(it) }
            if (files.size() > 1) {
                writeln "===== Select vocabulary file ====="
                Collections.reverse(files)
                files.eachWithIndex { f, i ->
                    writeln("  ${i}:\t${f.getName()}")
                }
                write "> "
                String answer = readAnswer()
                if (!answer.isEmpty()) {
                    int i = answer.toInteger()
                    VOCS_FILE = files[i]
                }
            }
        } else {
            createSampleFile()
            writeln "Info: Press ENTER to start training or CTRL+C / STRG+C to stop. "
            readAnswer()
        }
    }

    static void createSampleFile() {
        writeln "Info: creating sample vocabulary ${VOCS_FILE}", DELAY1
        StoreBox box = new StoreBox()
        box.newbies.add(new Word("go", "gehen, laufen"))
        box.newbies.add(new Word("swim", "schwimmen"))
        box.pending.add(new Word("make", "machen"))
        box.stored1.add(new Word("move", "bewegen"))
        box.stored2.add(new Word("add,put", "einfügen"))
        box.stored3.add(new Word("love", "die Liebe"))
        box.saveWords()
    }

    static void createEmptyVocsFile() {
        def doCreate = {
            writeln "Creating empty vocabulary file ${VOCS_FILE}", DELAY1
            new StoreBox().saveWords()
        }

        if (VOCS_FILE.exists()) {
            write "The file ${VOCS_FILE} already exists. Overwrite? [y/N] "
            def answer = readAnswer()
            if (answer == "y") {
                doCreate()
            }
        } else {
            doCreate()
        }
    }
}

class ConsoleUI
{
    static final Scanner scanner = new Scanner(System.in)
    static final int DELAY1 = 50
    static final int DELAY3 = 125
    static final int DELAY4 = 500
    static final int DELAY5 = 1000

    /** Reads user input case-insensitively by converting it to lower case. */
    static String readAnswer() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim().toLowerCase()
        } else {  // CTRL+D
            writeln "See ya!", DELAY1
            System.exit(1)
        }
    }

    static void write(String s, int delay = 0) {
        sleep delay
        print s
    }

    static void writeln(String s, int delay = 0) {
        write(s, delay)
        println ""
    }
}

class VocsFileException extends Exception {}

class Word
{
    // min and max values for passXY fields
    static final MIN_PASS = -1
    static final MAX_PASS = 10

    // regular expressions for parsing a word from a line that looks like this:
    // "walk;go (only at home!)/see that = gehen, laufen | 2008-04-06 | 2 | 1"
    static final RE_LANG = /[^=\|]+/              // all chars except '=' '|'
    static final RE_DATE = /\d\d\d\d-\d\d-\d\d/   // "2008-04-06"
    static final RE_NUM  = /-?\d+/                // "42", "-1"
    static final RE_OR   = /\s*\|\s*/             // '|' with optional surrounding space
    static final RE_EQ   = /\s*\=\s*/             // '=' with optional surrounding space
    static final LINE_PATTERN = ~/($RE_LANG)$RE_EQ($RE_LANG)($RE_OR($RE_DATE)$RE_OR($RE_NUM)$RE_OR($RE_NUM))?/

    String langF // foreign-language meaning
    String langN // native-language meaning
    Date date    // date on which that word was entered first

    int passFN   // times this word was correctly answered when asked F->N
    int passNF   // times this word was correctly answered when asked N->F

    static String toLine(Word word) {
        return "${word.langF} = ${word.langN} | ${DATE_FORMAT.format(word.date)} | ${word.passFN} | ${word.passNF}"
    }

    static Word fromLine(String line) {
        def matcher = LINE_PATTERN.matcher(line)
        if (!matcher.find()) {
            throw new VocsFileException()
        }
        String langF = matcher.group(1).trim()
        String langN = matcher.group(2).trim()
        if (matcher.group(3) != null) {
            Date date = DATE_FORMAT.parse(matcher.group(4))
            int passFN = matcher.group(5).toInteger()
            int passNF = matcher.group(6).toInteger()
            return new Word(langF, langN, date, passFN, passNF)
        }
        return new Word(langF, langN)
    }

    Word(String langF, String langN, Date date = new Date(), int passFN = 0, int passNF = 0) {
        this.langF = langF
        this.langN = langN
        this.date = date
        this.passFN = passFN
        this.passNF = passNF
    }

    int getPassXY() { return min(passFN, passNF) }

    void resetPasses() { passFN = passNF = 0 }

    void trimPasses() {
        if (passFN < MIN_PASS) { passFN = MIN_PASS }
        if (passFN > MAX_PASS) { passFN = MAX_PASS }

        if (passNF < MIN_PASS) { passNF = MIN_PASS }
        if (passNF > MAX_PASS) { passNF = MAX_PASS }
    }

    String toString() { return "${langF} = ${langN}" }

    boolean equals(Object o) {  return (this.toString() == o.toString()) }

    int hashCode() { return this.toString().hashCode() }
}

class Store extends LinkedList
{
    String name
    int sampleSize
    int passLimit

    Store(String name, int sampleSize, int passLimit) {
        this.name = name
        this.sampleSize = sampleSize
        this.passLimit = passLimit
    }

    Sample getSample() {
        Sample sample = new Sample(subList(0, calcSampleSize()))
        Collections.shuffle(sample)
        return sample
    }

    int calcSampleSize() { return min(sampleSize, size()) }
}

class StoreBox extends ArrayList
{
    // each word is in one of these lists
    Store newbies = new Store("[newbies]", 16, 2)  // newly entered words, not yet started
    Store pending = new Store("[pending]",  8, 2)  // started, but not yet learned words
    Store stored1 = new Store("[stored1]",  4, 1)  // recently learned words are stored here
    Store stored2 = new Store("[stored2]",  2, 1)  // older words are here
    Store stored3 = new Store("[stored3]",  1, 1)  // and the oldest are here

    Set<Word> transit
    Store transitFrom

    StoreBox() {
        addAll([newbies, pending, stored1, stored2, stored3])
    }

    int sessionSize = 0

    /**
     * Corrects the sample sizes of the last stores making them bigger,
     * in case that the first stores are empty
     */
    void adjustSampleSizes() {
        List sizes = this*.sampleSize
        int next = 0
        for (store in this) {
            if (!store.isEmpty()) {
                store.sampleSize = sizes[next++]
            }
        }

        for (store in this) {
            sessionSize += store.calcSampleSize()
        }
    }

    void saveWords() {
        writeln "Info: saving words to ${VOCS_FILE.getCanonicalPath()}", DELAY1
        def writer = new BufferedWriter(VOCS_FILE.newPrintWriter(CHARSET))
        writer.writeLine("# Last Update: ${new Date()}")
        writer.newLine()
        this.each { store ->
            writer.writeLine("===== Store: ${store.name} =====")
            store.each { word ->
                String line = Word.toLine(word)
                writer.writeLine(line)
            }
            writer.writeLine("")
        }
        writer.close()
    }

    void loadWords() {
        writeln "Info: loading words from ${VOCS_FILE.getCanonicalPath()}", DELAY1
        Store store = null
        int lineNr = 0
        VOCS_FILE.eachLine { line ->
            lineNr++
            if (line.startsWith("#")) {
                // ignore comments
            } else if (line.startsWith("===== Store: ")) {
                def storeName = line.split()[2]        // [......]
                store = this.find { it.name == storeName }
            } else if (!line.isEmpty()){
                try {
                    store.add(Word.fromLine(line))
                } catch (VocsFileException e) {
                    writeln "Error: couldn't understand a line in the vocabulary file: ${line} (line ${lineNr})"
                }
            }
        }
    }

    /** for debugging */
    void dumpWords() {
        this.each { store ->
            writeln "===== Store: ${store.name} ====="
            store.each { word ->
                println Word.toLine(word)
            }
        }
    }
}

class Sample extends LinkedList
{
    Set<Word> wrongs = new HashSet()

    Sample(Collection words) {
        super(words)
    }
}

class Trainer
{
    StoreBox box

    void trainUser() {
        box.adjustSampleSizes()
        Set<Word> passers
        Store origin
        int storeNr = 1
        writeln "Info: starting new training session with ${box.sessionSize} words", DELAY3
        writeln "Info: to abort press CTRL+C", DELAY3
        double progress = 0
        for (store in box) {
            // get the sample for the current store
            Sample sample = store.getSample()

            // - move the transit words from the previous store into this store
            // - save the box after every store
            if (!store.is(box.newbies)) {
                origin.removeAll(passers)
                store.addAll(passers)
                passers.each { word -> word.resetPasses() }
                box.saveWords()
            }

            progress += Math.floor(sample.size() / box.sessionSize * 100)
            writeln "\n===== Using Store:  ${store.name}  (${progress} %) =====", (store.is(box.newbies) ? DELAY1 : DELAY5)
            if (store.isEmpty() || sample.isEmpty()) {
                writeln "... it's empty"
            }

            if (store.is(box.newbies)) {
                askSampleFN(sample)
                askSampleNF(sample)
            }
            else {
                askSampleRandom(sample)
            }
            writeln ""

            Set<Word> wrongs = sample.wrongs
            Set<Word> rights = sample - wrongs
            Set<Word> leaving = rights.findAll { it.getPassXY() >= store.passLimit }
            Set<Word> staying = rights - leaving

            // move wrongly guessed words to first store
            store.removeAll(wrongs)
            box.newbies.addAll(wrongs)

            // prepare transit words for next store
            passers = leaving
            origin = store

            // move staying words to the end of this store
            store.removeAll(staying)
            store.addAll(staying)

            // move transit words at the end if we are at the last store
            if (store.is(box.stored3)) {
                store.removeAll(passers)
                store.addAll(passers)
            }
        }
        box.saveWords()
        write "\nInfo: training is complete. Press ENTER to exit: "
        readAnswer()
    }

    void askSampleFN(Sample sample) {
        if (sample.isEmpty()) return
        writeln "\nPractice:  [foreign -> native]  (${sample.size()} words)", DELAY4
        askSampleXY(sample, "langF", "langN", "passFN")
    }

    void askSampleNF(Sample sample) {
        if (sample.isEmpty()) return
        writeln "\nPractice:  [native -> foreign]  (${sample.size()} words)", DELAY4
        askSampleXY(sample, "langN", "langF", "passNF")
    }

    void askSampleXY(Sample sample, String langX, String langY, String passXY) {
        for (word in sample) {
            askWord(word, langX, langY, passXY, sample.wrongs)
        }
    }

    boolean askWord(Word word, String langX, String langY, String passXY, Set<Word> wrongs) {
        write   "\n> ${word[langX]} = ", DELAY4
        boolean right
        String answer = readAnswer()
        if (answer.isEmpty()) {
            writeln "  ${word[langY]}", DELAY3
            write   "  Guessing without typing? Was it right or wrong? [R/w] ", DELAY3
            answer = readAnswer()
            right = ((answer == "r") || answer.isEmpty())
        } else {
            right = word[langY].toLowerCase().contains(answer)
            if (right) {
                if (word[langY].size() != answer.size()) {
                    writeln "  ${word[langY]}", DELAY3
                }
            } else {
                writeln "  ${word[langY]}", DELAY3
                write "  This looks wrong... Was it right or wrong? [r/W] ", DELAY3
                answer = readAnswer()
                right = (answer == "r")
            }
        }

        if (right) {
            writeln "  :)", DELAY3
            word[passXY] ++
        } else {
            writeln "  :(", DELAY3
            word[passXY] --
            wrongs.add(word)
        }
        word.trimPasses()
        return right
    }

    void askSampleRandom(Sample sample) {
        if (sample.isEmpty()) return
        writeln "\nPractice:  [random <-> random]  (${sample.size()} words)", DELAY4
        List doubleSample
        if (sample.size() > 2) {
            int middle = sample.size() / 2
            List left = new ArrayList(sample.subList(0, middle))
            List right = new ArrayList(sample.subList(middle, sample.size()))
            shuffle(left)
            shuffle(right)
            doubleSample = sample + left + right
        } else {
            doubleSample = sample + sample
        }

        def fieldDirs = [["langF", "langN", "passFN"],
                         ["langN", "langF", "passNF"]]
        Map<Word, Integer> asked = [:]
        int dirCounter = 0
        for (word in doubleSample) {
            def fields
            if (!asked.containsKey(word)) {
                int dirIndex = dirCounter++ % 2
                asked[word] = dirIndex
                fields = fieldDirs[dirIndex]
            } else {
                int dirIndex = asked[word]
                fields = fieldDirs[(dirIndex + 1) % 2]
            }
            askWord(word, fields[0], fields[1], fields[2], sample.wrongs)
        }
    }

    void shuffle(Collection c) {
        if (c.size() > 1) {
            Collection orig = c.clone()
            while (c == orig) {
                Collections.shuffle(c)
            }
        }
    }
}

// main(args)

Settings.initialize()
switch (args.size()) {
    case 2:
        if (args[0] == "create") {
            VOCS_FILE = new File(VOCS_DIR, "vocabulary.${args[1]}.txt")
            Settings.createEmptyVocsFile()
            return
        }
        break
    case 1:
        File try1 = new File(VOCS_DIR, args[0])
        File try2 = new File(VOCS_DIR, "vocabulary.${args[0]}.txt")
        VOCS_FILE = [try1, try2].find { it.exists() }
        if (VOCS_FILE == null) {
            writeln "The specified vocabulary file doesn't exist"
            return
        }
        break
    case 0:
        Settings.selectVocsFile()
        break
}
StoreBox box = new StoreBox()
box.loadWords()
trainer = new Trainer(box:box)
trainer.trainUser()

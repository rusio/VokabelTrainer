A small command-line vocable trainer that uses the [Leitner Box System](https://en.wikipedia.org/wiki/Leitner_system).

The vocables are stored into 5 "boxes", where they are grouped
by learning progress. To run you will need Java and Groovy installed.

Installation
============

Installing Java and Groovy
--------------------------

Vokabeltrainer is written in Groovy and needs a Groovy Interpreter
and a Java Runtime Environment in order to run.

- [Java](http://www.java.com/en/download/)
- [Groovy](http://groovy-lang.org/download.html)

Test the installation, by opening a console and typing:

    java -version
    groovy -version

You should see version information for both commands.

Installing VokabelTrainer
-------------------------

Extract the archive file into C:\Programme. You should now have
the following folder:

    C:\Programme\VokabelTrainer-x.y


Basic Usage
===========

Starting VokabelTrainer
-----------------------

On Windows there are 2 ways to run VokabelTrainer:

- Double-click on the file vokabeltrainer.groovy in the program folder.

- Open a console (Command Prompt/Eingabeaufforderung) and use this command:


    groovy C:\Programme\vokabeltrainer-x.y\vokabeltrainer.groovy

On the first startup VokabelTrainer asks for a directory where it will store the
vocabulary files (default is the your home directory). Then a new vocabulary file
with some sample data will be created.

    C:\Users\user\vocabulary.txt

Stopping VokabelTrainer
-----------------------

In a typical training session VokabelTrainer asks you about 30 words in various
directions and and terminates. You can terminate the session yourself by pressing
`CTRL+C`.

Note: temporary results will be saved after a store is completed.


Managing Your Vocables
======================

The vocables are stored in the `vocabulary.txt` file, which is located in your home
folder by default. You can edit this file with a text editor and add/change/remove
vocables. To enter new words, open the file and add a new line for each word on the
top of the newbies-store:

     ===== Store: newbies =====

     dog = Katze                               <- new words are added like this
     cow = Kuh  | 2008-04-13 | 2 | 0           <- existing word
     man = Mann | 2008-04-13 | 2 | 0           <- existing word

You only need to enter both meanings of the word separated by a `=`.
The additional statistical data will be added by VokabelTrainer automatically.

After some training sessions on each line there will be statistical information

- when was the word first entered
- how many times was the word guessed right in the (foreign->native) direction
- how many times was the word guessed right in the (native->foreign) direction


Training Vocables
=================

Before starting training with new words you should know them a little bit.
In a training session your answers(right and wrong) will be counted and on that
base words will be moved up or down from one store to another.

Answering the Questions
-----------------------

In a training session you will be asked words from all stores. You can state your
answer by two ways:

- By writing the answer. That way you can exercise writing the word.
  Here, you have to match the way you entered it in the file, or at least a
  smaller part of it - "kat" will be correct, even if the complete answer would
  be "Die Katze". But if you type "kadze" instead of "katze" it will be wrong.

- By just pressing Enter. This is good if you want to write the word with a pen
  to paper, instead of typing it, or if you already know the word in your head
  and don't want to write it, or if you just want to guess.

Scoring Rules
-------------

The words in the vocabulary file are grouped in 5 logical sections called stores.
The upper stores contain the newer words, while the lower stores contain the older
and better memorized words.

The most words in a training session are taken from the first store, where the new
words reside. The older a word is, the lower is the chance that it will be asked.
But sooner or later it will be asked definitely.

When a word is guessed correctly a couple of times, it is automatically moved down
to a deeper store. But when it is guessed wrongly, it moves up into the first store!
That way you train the words you don't know well or have forgotten more intensively
than words you know well. The following table shows some of the rules:

    Store     Asked Words     Asked Order            To Move Deeper
    -----     -----------     -----------            -------------
    newbies       16          f->n, f<-n, f<->n      2 correct guesses for both directions
    pending        8          f<->n                  2 correct guesses for both directions
    stored1        4          f<->n                  1 correct guess for both directions
    stored2        2          f<->n                  1 correct guess for both directions
    stored3        1          f<->n


Additional Configuration
========================

VokabelTrainer uses a configuration file for it's settings, which is created on the first
use in your home directory.

    C:\Users\user\vocabulary.txt

Multiple Vocabulary Files
-------------------------

You can have multiple files for your vocabulary. That enables you to use the program
for learning more than one foreign language. Furthermore, you can have special files
for specials vocables like irregular verbs. To have another vocabulary file, copy and
rename the default file (vocabulary.txt) or create the new file with the command:

    groovy vokabeltrainer.groovy create es-bg

This will create a new file next to your default vocabulary file

    C:\Users\user\vocabulary.txt              # the default file
    C:\Users\user\vocabulary.es-bg.txt        # Spanish-Bulgarian

The new files should contain these lines:

     ===== Store: newbies =====

     ===== Store: pending =====

     ===== Store: stored1 =====

     ===== Store: stored2 =====

     ===== Store: stored3 =====

When you run Vokabeltrainer it will ask you which file to use.
You can also specify which file to use on the command line:

    groovy vokabeltrainer.groovy vocabulary.es-bg.txt
    groovy vokabeltrainer.groovy es-bg

Changing the Location of the Vocabulary Files
---------------------------------------------

If you like to store your vocabulary files not in your home directory, but
into a different location, then change the entry in the configuration file.
Note: enter the full path to the directory by using double backslashes '\\'
or sinle slashes '/' as a path-separator! Examples:

    VOCS_DIR=C:\\Users\\user     (Windows)
    VOCS_DIR=/home/asdf01        (Linux)


Current Issues
==============

Unicode Characters
------------------

On Windows it is problematic to display and enter the characters of the foreign language.
I don't know how to solve this yet, if you know a solution, please send me an Email!
A workarround is to write the foreign words with a phonetic transcription.

"must=mussen"

instead of

"must=müssen"

--
Mabye try this: regional settings, advanced, language for non-unicode applications, bulgarian
--


Bugs, Improvements, New Features
================================

Please send me an Email if you find a bug, or have an idea for improving
VokabelTrainer! dolvich at users.sourceforge.net


Change Log
==========

Release 1.3
-----------

New Features:
- location of directory with vocabulary files is now configurable

Improvements:
- parsing of lines in vocabulary file is now more robust and flexible
- progress of training session is shown
- enhanced initial setup procedure

Changes:
- word-separator in vocabulary-file is now '=' instead of '--'

Bug-Fixes:
- parsing negative numbers (-1) in vocabulary file
- initial showup of '>' on startup is fixed
- showing absolute path on load/save


Next Steps
==========

- Add Feature: show training session progress periodically (10%, 20% etc.)

- Add Feature? distinguish between important/unimportant hard/easy words
  and ask them more/less frequently?

- Change? use only random phase on store 1? the other phases make it very long

- 2 modi: learning and training(only random) with learn-mode optional

- Add Feature: make sample sizes configurable

- Add Feature: make learning-mode configurable "practicemode=learning|training"



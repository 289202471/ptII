# Makefile for Ptolemy II Javasound actors
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2008 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/actor/lib/javasound

DIRS = 		test demo

# Root of the Java directory
ROOT =		../../../..

# This directory might get built before ptolemy.actor.injection, so we include guice
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTINJECTION_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	javasound
PTCLASSJAR =	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AudioReader.java \
	AudioWriter.java \
	AudioCapture.java \
	AudioPlayer.java \
	AudioReadBuffer.java \
	AudioWriteBuffer.java \
	ClipPlayer.java \
	LiveSoundActor.java \
	SoundActor.java \
	SoundPlayer.java

# We include voice.wav so that we can include the voice.wav file in
# the jar file for use under Web Start.
OTHER_FILES_TO_BE_JARED = \
	AudioCaptureIcon.xml \
	AudioPlayerIcon.xml \
	gong.wav \
	javasound.xml \
	voice.wav

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk


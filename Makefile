# 
# Copyright 2015 Naver
# Author : Dongseok Hyun <dustin.hyun@navercorp.com>
#
# This file is part of Usher.
#
# Usher is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Usher is distrubuted in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Usher. If not, see <http://www.gnu.org/licenses/>.
#
# Makefile
#
# Builds Usher.war, UsherTest
#

OUTDIR=out
SERVLET_OUTDIR=servlet-out
TOMCAT_LIB='UPDATE HERE/apache-tomcat/lib'

LIBS=.:${TOMCAT_LIB}/servlet-api.jar:./lib/gephi-toolkit.jar:./lib/json-simple-1.1.1.jar

TEST_CLASSPATH='${OUTDIR}:./lib/gephi-toolkit.jar:./lib/json-simple-1.1.1.jar'

all:
	javac -cp '${LIBS}' UsherServlet.java UsherTest.java Usher.java
	mkdir -p ${OUTDIR}
	mkdir -p \
		${SERVLET_OUTDIR}/WEB-INF/classes \
		${SERVLET_OUTDIR}/WEB-INF/lib \
		${SERVLET_OUTDIR}/data
	cp web.xml ${SERVLET_OUTDIR}/WEB-INF/.
	mv UsherServlet.class ${SERVLET_OUTDIR}/WEB-INF/classes/.
	cp Usher.class ${SERVLET_OUTDIR}/WEB-INF/classes/.
	mv Usher.class UsherTest.class ${OUTDIR}/.
	cp lib/*.jar ${SERVLET_OUTDIR}/WEB-INF/lib/.
	cd ${SERVLET_OUTDIR}; jar -cvf Usher.war .; cd ..

test:
	java -cp ${TEST_CLASSPATH} UsherTest

clean:
	rm -rv out servlet-out

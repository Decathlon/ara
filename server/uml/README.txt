Here are instructions to manually generate the UML diagram image.

===================================================================================================
THIS IS NOT NEEDED ANYMORE, AS THE uml.txt FILE IN INCLUDED IN THE ASCIIDOC OF THE PROJECT.
See README.adoc at the root of the project to know how to generate the documentation.
And see README.adoc at the root of the server module for the UML integrated into the documentation.
Of course, the uml.txt file needs to be updated, as it is the source of the project documentation.
===================================================================================================

Using "Wifi Gratuit Decathlon" for no proxy problem

Download and install GraphViz: +
http://www.graphviz.org/download/ +
Choose eg. "Stable 2.38 Windows install packages"

Download and install "Ruby 2.4.1-1 (x64)":
https://rubyinstaller.org/downloads/

Install msys64 when asked by Ruby installer

in msys64:
export PATH="/C/Ruby24-x64/bin:$PATH"
gem install asciidoctor-diagram

Successfully installed asciidoctor-1.5.5
Successfully installed asciidoctor-diagram-1.5.4
Parsing documentation for asciidoctor-1.5.5
Installing ri documentation for asciidoctor-1.5.5
Parsing documentation for asciidoctor-diagram-1.5.4
Installing ri documentation for asciidoctor-diagram-1.5.4
Done installing documentation for asciidoctor, asciidoctor-diagram after 3 seconds
2 gems installed

cd ~/git/ara/server/uml
export GRAPHVIZ_DOT=/C/Program\ Files\ \(x86\)/Graphviz2.38/bin/dot.exe
export PATH="/C/Program Files (x86)/Graphviz2.38/bin:$PATH"
asciidoctor -r asciidoctor-diagram uml.adoc
gem install ruby-graphviz

Download https://sourceforge.net/projects/plantuml/files/plantuml.jar/download?SetFreedomCookie

export PATH="/C/Program Files/Java/jdk1.8.0_121/bin:$PATH"
cd ~/git/ara/server/uml
java -jar plantuml.jar uml.txt

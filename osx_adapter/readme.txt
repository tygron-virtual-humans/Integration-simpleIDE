/**
 * GOAL interpreter that facilitates developing and executing GOAL multi-agent
 * programs. Copyright (C) 2011 K.V. Hindriks, W. Pasman
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */


The OSXAdapter is a special OSX dependent java file containing OSX dependent tweaks.

You can compile this only on OSX.
It can be included with any system but it can be INSTANTIATED only on OSX.
So you should test if you are on OSX before instantiating, by doing this somethign like:

		if (System.getProperty("os.name").equals("Mac OS X")) {
			try {
				Class<?> adapterclass = Class.forName("osxadapter.Adapter");
				Constructor adapterconstructor = adapterclass.getConstructor(IDEfunctionality.class);
				myosxadapter = adapterconstructor.newInstance(this);
			} catch (Exception e) {
				new Warning("Loading of OSX specific functionality failed, continuing without");
			}
		}
		
Compile this with 

cd osx_adapter
javac -classpath .:../bin:../dep/goal.jar osxadapter/Adapter.java
jar cf osxadapter.jar osxadapter/*.class


Note, we do not compile straight to the bin directory of SimpleIDE because a 
clean-project command in Eclipse would then erase the compiled files
and then people on windows would loose essential files.
(and they would not even notice that they lost them I guess...)

The consequence of that is that you now have to add osxadapter to the
library path of the Eclipse project with

Project properties/Libraries/Add Class Folder/osx_adapter

Another consequence is that we explicitly need to copy the class files into 
the installer (a job to be done by the installer).
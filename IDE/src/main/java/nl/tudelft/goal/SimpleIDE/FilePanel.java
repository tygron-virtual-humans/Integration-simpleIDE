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

package nl.tudelft.goal.SimpleIDE;

import goal.preferences.PMPreferences;
import goal.tools.PlatformManager;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALIncompleteGUIUsageException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.util.Extension;
import goal.util.IterableStitcher;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import krTools.KRInterface;
import krTools.errors.exceptions.ParserException;
import languageTools.program.agent.AgentProgram;
import languageTools.program.mas.MASProgram;
import nl.tudelft.goal.SimpleIDE.actions.CloseAndRemoveAction;
import nl.tudelft.goal.SimpleIDE.actions.DeleteAction;
import nl.tudelft.goal.SimpleIDE.actions.EditAction;
import nl.tudelft.goal.SimpleIDE.actions.NewFileAction;
import nl.tudelft.goal.SimpleIDE.actions.OpenFileAction;
import nl.tudelft.goal.SimpleIDE.actions.QuitAction;
import nl.tudelft.goal.SimpleIDE.actions.ReloadFileAction;
import nl.tudelft.goal.SimpleIDE.actions.RenameAction;
import nl.tudelft.goal.SimpleIDE.actions.RunAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveFileAction;
import nl.tudelft.goal.SimpleIDE.files.FileNode;
import nl.tudelft.goal.SimpleIDE.files.GOALNode;
import nl.tudelft.goal.SimpleIDE.files.MASNode;
import nl.tudelft.goal.SimpleIDE.files.ModulesNode;
import nl.tudelft.goal.SimpleIDE.files.NullNode;
import nl.tudelft.goal.SimpleIDE.files.PrologNode;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Panel that displays the loaded .goal and .mas files as IDENodes. Displays a
 * list of {@link NodeType#MASFILE}s with their respective
 * {@link NodeType#GOALFILE}s, as well as a list of spurious goal files below a
 * {@link NodeType#NULLFILE}. Allows the user to select and edit files.<br>
 * Below each {@link NodeType#GOALFILE} will be its imported files, but only
 * once the user saves the agent file.
 *
 * @author K.Hindriks
 * @author N.Kraayenbrink rewritten 30 apr 2010, see trac #1062
 *
 */
public class FilePanel extends JPanel {

	/** Auto-generated serial version UID */
	private static final long serialVersionUID = 2394932969608968176L;

	private final IDEState ideState; // final too

	private final JTree fileTree;
	private final DefaultTreeModel treeModel;
	private final FileNode rootNode;
	private final FileNode projectsNode; // the node for MAS projects
	private final NullNode nullNode; // the node for unused files
	private final JPanel mainpanel;
	/**
	 * DOC
	 */
	private final FileNodeMap allFiles;

	private final PlatformManager platform;

	/**
	 * DOC
	 *
	 * @param mainp
	 *            the main panel, for centering dialogs.
	 * @param state
	 *            the IDE state
	 */
	public FilePanel(JPanel mainp, IDEState state) {

		this.mainpanel = mainp;
		this.ideState = state;
		this.rootNode = new FileNode(NodeType.ROOT, null);
		this.rootNode.setAllowsChildren(true);
		this.projectsNode = new FileNode(NodeType.ROOT,
				new File("MAS Projects")); //$NON-NLS-1$
		this.nullNode = new NullNode();
		this.rootNode.add(this.projectsNode);
		this.rootNode.add(this.nullNode);

		this.treeModel = new DefaultTreeModel(this.rootNode, true);
		this.fileTree = new JTree(this.treeModel);

		// add the various event listeners
		addListeners();

		// double-click should not toggle the tree
		this.fileTree.setToggleClickCount(-1);

		// set the layout
		setLayout(new BorderLayout());
		this.fileTree.setEditable(false); // TODO: allow this.
		this.fileTree.setRootVisible(false);
		this.fileTree.setShowsRootHandles(true);
		this.fileTree.setCellRenderer(new IDENodeRenderer());
		// include tree view in pane
		JScrollPane fileTreeView = new JScrollPane(this.fileTree);
		this.add(new JLabel("Files"), BorderLayout.NORTH); //$NON-NLS-1$
		this.add(fileTreeView, BorderLayout.CENTER);

		this.allFiles = new FileNodeMap();

		this.platform = PlatformManager.getCurrent();
	}

	private JPopupMenu createPopupMenu() throws IllegalAccessException,
			InstantiationException {
		JPopupMenu popup = new JPopupMenu();
		popup.add(new JMenuItem(ActionFactory.getAction(EditAction.class)));
		popup.add(new JMenuItem(ActionFactory.getAction(SaveFileAction.class)));
		popup.add(new JMenuItem(ActionFactory.getAction(ReloadFileAction.class)));
		popup.add(new JMenuItem(ActionFactory
				.getAction(CloseAndRemoveAction.class)));
		popup.add(new JMenuItem(ActionFactory.getAction(RenameAction.class)));
		popup.add(new JSeparator());
		popup.add(new JMenuItem(ActionFactory.getAction(OpenFileAction.class)));
		popup.add(new JMenuItem(ActionFactory.getAction(NewFileAction.class)));
		popup.add(new JMenuItem(ActionFactory.getAction(DeleteAction.class)));
		popup.add(new JSeparator());
		popup.add(new JMenuItem(ActionFactory.getAction(RunAction.class)));
		popup.add(new JSeparator());
		popup.add(new JMenuItem(ActionFactory.getAction(QuitAction.class)));
		return popup;

	}

	/**
	 * Adds all relevant listeners to this FilePanel's tree.
	 */
	private void addListeners() {
		// add mouse listener
		this.fileTree.addMouseListener(new myMouseListener());

		// add tree selection listener
		this.fileTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// theIDE.refreshMenuItemsAndButtons();
				ActionFactory.broadcastStateChange(FilePanel.this.ideState);
			}
		});
		// tree expansion listener
		this.fileTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				// theIDE.refreshMenuItemsAndButtons();
				ActionFactory.broadcastStateChange(FilePanel.this.ideState);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				// theIDE.refreshMenuItemsAndButtons();
				ActionFactory.broadcastStateChange(FilePanel.this.ideState);
			}
		});
	}

	/**
	 * This object handles mouse clicks in the panel. Its jobs:
	 * <ul>
	 * <li>Create popup menu
	 * <li>Handle double click to open editor.
	 *
	 * @author W.Pasman 18jul2011
	 *
	 */
	private class myMouseListener extends MouseAdapter {
		/**
		 * Handles double click events on file nodes, in order to open editor
		 * panels for the file(s) that are selected. Single click events are
		 * handled by the tree selection listener.
		 *
		 * @see http ://java.sun.com/j2se/1.4.2/docs/api/javax/swing/JTree.html
		 *      for code.
		 */
		@Override
		public void mousePressed(MouseEvent event) {
			if (event.isPopupTrigger()) {
				try {
					createPopupMenu().show(FilePanel.this.fileTree,
							event.getX(), event.getY());
				} catch (Exception e) {
					new Warning(
							Resources
									.get(WarningStrings.FAILED_POPUP_WINDOW_CREATE),
							e);
				}
			}
			TreePath selPath = FilePanel.this.fileTree.getPathForLocation(
					event.getX(), event.getY());
			// path is null if nothing is selected
			if (selPath == null) {
				return;
			}
			FileNode node = (FileNode) selPath.getLastPathComponent();
			if (node != null && event.getClickCount() == 2
					&& !(node.equals(FilePanel.this.nullNode))) {
				// user double clicked process node
				try {
					ActionFactory.getAction(EditAction.class).Execute(node,
							null);
				} catch (Exception e) {
					new Warning(Resources.get(WarningStrings.FAILED_FILE_EDIT),
							e);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (event.isPopupTrigger()) {
				try {
					createPopupMenu().show(FilePanel.this.fileTree,
							event.getX(), event.getY());
				} catch (Exception e) {
					new Warning(
							Resources
									.get(WarningStrings.FAILED_POPUP_WINDOW_CREATE),
							e);
				}
			}
		}
	}

	/**
	 * Get ALL selected {@link FileNode}s in the panel's tree, on top-to-bottom
	 * order. See #545. If nothing is selected, returns list with the root node
	 * only.
	 *
	 * @return list of all selected nodes.
	 */
	public List<IDENode> getSelectedNodes() {
		TreePath[] paths = this.fileTree.getSelectionPaths();
		List<IDENode> nodes = new ArrayList<IDENode>();
		if (paths == null) {
			nodes.add(this.rootNode);
		} else {
			for (TreePath path : paths) {
				nodes.add((FileNode) path.getLastPathComponent());
			}
		}
		return nodes;
	}

	/**
	 * Checks if this FilePanel contains a reference to the given file.
	 *
	 * @param file
	 *            The file that may or may not be referenced in this panel.
	 * @return {@code true} iff there is a reference to the given file in this
	 *         panel.
	 */
	public boolean containsFile(File file) {
		return this.allFiles.containsKey(file);
	}

	/**
	 * Inserts a file into the tree in this {@link FilePanel}. If the given file
	 * is a MAS file, the new file will be located directly below the root node
	 * as its child. A GOAL file is either located under a MAS file or under the
	 * null node, depending on what node is selected in the tree. If a MAS file
	 * is selected (or any of its child GOAL files), the new GOAL file will be
	 * located below that MAS file. If not, the new GOAL file will be located
	 * under the null-node.
	 *
	 * @param newFile
	 *            The File to add to the tree in this {@link FilePanel}.
	 * @return <ul>
	 *         <li>The newly created {@link FileNode} when no node with the
	 *         given file existed yet.</li>
	 *         <li>An already existing {@link FileNode} if one with the given
	 *         {@link File} was already present</li>
	 *         <li><code>null</code> iff something went wrong. Information has
	 *         already been printed.</li>
	 *         </ul>
	 * @throws GOALException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParserException
	 */
	public FileNode insertFile(File newFile) throws IllegalAccessException,
			InstantiationException, GOALException, ParserException {
		FileNode newNode = null;

		Extension ext = Extension.getFileExtension(newFile);
		if (ext == null) {
			newNode = new FileNode(NodeType.TXTFILE, newFile);
			this.allFiles.add(newNode);
			appendNode(null, newNode);
			refreshSpuriousList();
		} else {
			switch (ext) {
			case MAS:
				newNode = insertMASfile(newFile);
				List<File> agentFiles = this.platform.getMASProgram(
						newNode.getBaseFile()).getAgentFiles();
				for (File agentfile : agentFiles) {
					refreshGOALFile(agentfile);
				}
				break;
			case GOAL:
				newNode = insertGOALfile(newFile);
				break;
			case MODULES:
				newNode = new ModulesNode(newFile);
				appendNode(null, newNode);
				this.allFiles.add(newNode);
				refreshSpuriousList();
				break;
			case PROLOG:
				newNode = new PrologNode(newFile);
				appendNode(null, newNode);
				this.allFiles.add(newNode);
				refreshSpuriousList();
				break;
			default:
				throw new UnsupportedOperationException("Unhandled Extension " //$NON-NLS-1$
						+ ext);
			}
		}
		if (newNode != null) {
			selectNode(newNode);
		}
		return newNode;
	}

	/**
	 * Inserts a new MAS node in this {@link FilePanel}'s tree. If the given
	 * file is already present in the tree, the sub-tree is refreshed for the
	 * corresponding MASNode. Otherwise a new {@link MASNode} is created and
	 * appended at the end (or before the null-node if it is visible).<br>
	 * The MAS-file is parsed and all relevant child GOAL-files are appended.
	 *
	 * @param newMASfile
	 *            The new .mas-file to append.
	 * @return The (possibly new, possibly old) {@link MASNode} with a link to
	 *         the given file.
	 * @throws GOALException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParserException
	 */
	private MASNode insertMASfile(File newMASfile)
			throws IllegalAccessException, InstantiationException,
			GOALException, ParserException {

		List<FileNode> newNodeList = this.allFiles.getAll(newMASfile);
		MASNode newNode = null;
		// DOC: check whether node for MAS file already exists???
		if (newNodeList != null && newNodeList.size() > 0) {
			newNode = (MASNode) newNodeList.get(0);
		}

		// MAS file does not have corresponding node yet; needs to be loaded and
		// parsed.
		if (newNode == null) {
			// Load and parse MAS file.
			this.platform.parseMASFile(newMASfile);

			// create a new node, and add it to the mapping.
			newNode = new MASNode(newMASfile);
			this.allFiles.add(newNode);

			// add the new node at the end. The null-node will be reset in
			// the #refreshMASNode call.
			appendNode(this.projectsNode, newNode);
		} else {
			ActionFactory.getAction(ReloadFileAction.class).Execute(newNode,
					null);
		}

		// Refresh the tree so that the children of the MAS node are reset.
		refreshMASNode(newNode);

		return newNode;
	}

	/**
	 * Inserts a new GOAL node in this {@link FilePanel}'s tree. If the given
	 * file is already present in the tree, a reload command is sent to the IDE
	 * to refresh the editor, thus reloading the .goal file in the editor if it
	 * was open.
	 * <p>
	 * The new location of the goal file in the tree depends on the current
	 * selection. If a MAS node is selected (or any of its child goal nodes),
	 * the new node will be its child. If anything else is selected, the new
	 * goal file will become a child of the null file node, which is then made
	 * visible if it wasn't already.
	 * </p>
	 *
	 * FIXME implementation<->documentation mismatch.
	 *
	 * @param newGOALfile
	 *            The agent (.goal) file to append.
	 * @return The (possibly new, possibly old) {@link GOALNode} with a link to
	 *         the given file.
	 */
	private GOALNode insertGOALfile(File newGOALfile) {

		List<FileNode> gNodeSet = this.allFiles.getAll(newGOALfile);
		GOALNode newNode = null;
		if (gNodeSet != null && !gNodeSet.isEmpty()) {
			newNode = (GOALNode) gNodeSet.get(0);
		}

		if (newNode == null) {
			// create a new node and add it to the mapping
			newNode = new GOALNode(newGOALfile);
			this.allFiles.add(newNode);

			// add the new goal file node to the tree
			// the new goal file is always inserted below the null file
			appendNode(this.nullNode, newNode);
		} else {
			// reload the .goal file in the editor (if it is open)
			// TODO: use command that fails silently.
			selectNode(newNode);
			try {
				ActionFactory.getAction(ReloadFileAction.class).Execute(
						newNode, null);
			} catch (Exception e) {
				new Warning(String.format(
						Resources.get(WarningStrings.FAILED_FILE_RELOAD),
						newGOALfile.getAbsolutePath()), e);
			}
		}
		return newNode;
	}

	/**
	 * Adds a certain file to the list of 'Other Files'. Note that calling this
	 * does not completely refresh the list, and may contain duplicates or
	 * non-spurious files. Call {@link #refreshSpuriousList()} to properly
	 * refresh it.
	 *
	 * @param file
	 *            The file to add to the list of spurious files.
	 * @return The newly created {@link FileNode} referencing the given
	 *         {@link File}.
	 */
	public FileNode insertSpuriousFile(File file) {
		Extension ext = Extension.getFileExtension(file);
		FileNode newNode = null;
		if (ext == null) {
			newNode = new FileNode(NodeType.TXTFILE, file);
		} else {
			switch (ext) {
			case GOAL:
				newNode = new GOALNode(file);
				break;
			case MAS:
				throw new UnsupportedOperationException("Attempt to" //$NON-NLS-1$
						+ "insert MAS file as spurious file in File Panel."); //$NON-NLS-1$
			case MODULES:
				newNode = new ModulesNode(file);
				break;
			case PROLOG:
				newNode = new PrologNode(file);
				break;
			}
		}
		this.allFiles.add(newNode);
		appendNode(null, newNode);
		return newNode;
	}

	/**
	 * Appends a node to the children of another node. Shorthand for two calls
	 * to {@link #treeModel}.<br>
	 * If parent is null, the child is removed from its parent (if it has one).
	 * If the node was not a child of the {@link #nullNode}, it will be moved
	 * there. Otherwise, this call would be the same as {@link #removeFileNode}.
	 *
	 * @param parent
	 *            The desired parent of the given child.
	 * @param child
	 *            The node that is to be appended to the list of children of the
	 *            given parent.
	 */
	private void appendNode(FileNode parent, FileNode child) {
		boolean oldParentWasNullNode = child.getParent() == this.nullNode;
		if (child.getParent() != null) {
			this.treeModel.removeNodeFromParent(child);
		}
		if (parent != null) {
			this.treeModel
					.insertNodeInto(child, parent, parent.getChildCount());
			this.fileTree.expandPath(new TreePath(child.getPath()));
		} else if (!oldParentWasNullNode && child.getType() != NodeType.MASFILE) {
			// automatically move the old node to the null-node when it
			// has been removed, unless it is a MAS-file
			this.treeModel.insertNodeInto(child, this.nullNode,
					this.nullNode.getChildCount());
			this.fileTree.expandPath(new TreePath(child.getPath()));
			refreshSpuriousList();
		}
	}

	/**
	 * Removes the given filenode from the tree. This will not send the node to
	 * the {@link #nullNode}, as {@link #appendNode}<code>(null, child)</code>
	 * does.
	 *
	 * @param child
	 *            The node to remove from the tree.
	 */
	private void removeNodeFromTree(FileNode child) {
		if (child.getParent() != null) {
			this.treeModel.removeNodeFromParent(child);
		}
	}

	/**
	 * Selects a single node in the tree, and expands the tree so that the given
	 * node is visible. Assumes the given node is inside the tree.
	 *
	 * @param node
	 *            The node to be selected. Can be <code>null</code>, in which
	 *            case the result will be that nothing is selected.
	 */
	private void selectNode(FileNode node) {
		if (node == null) {
			this.fileTree.setSelectionPath(null);
		} else {
			TreePath selectedPath = new TreePath(node.getPath());
			this.fileTree.setSelectionPath(selectedPath);
			this.fileTree.expandPath(selectedPath);
		}
	}

	/**
	 * Updates the tree model so that the children of the given mas node
	 * correspond to the agent files described in the mas file. Any goal files
	 * that are children of the given mas node but are not referenced to in the
	 * mas file will be moved to the null file node.
	 *
	 * @param masNode
	 *            The mas node of which the children should be refreshed.
	 * @param showLoadError
	 *            If this is <code>false</code>, any GOALException thrown when
	 *            getting the agent file names will be ignored. If
	 *            <code>true</code>, the user will be notified of them.<br>
	 *            Use <code>false</code> when this is called after saving, as
	 *            the message should have already been displayed.
	 */
	private void refreshMASNode(MASNode masNode) {

		// Do nothing if the given node is not part of the tree (or null)
		if (!this.rootNode.isNodeDescendant(masNode)) {
			return;
		}

		// Do nothing if the file is not validated.
		MASProgram mas = this.platform.getMASProgram(masNode.getBaseFile());
		if (mas == null) {
			return;
		}

		// Compare current nodes with current agent files associated with MAS
		// file.
		// Get current node children.
		List<FileNode> currentChildren = getChildrenOf(masNode);
		// Get the associated files.
		List<File> currentFiles = new ArrayList<File>();
		for (FileNode node : currentChildren) {
			currentFiles.add(node.getBaseFile());
		}

		// Get new agent files.
		List<File> newFiles = this.platform
				.getMASProgram(masNode.getBaseFile()).getAgentFiles();

		// Check whether nodes need to be removed, i.e., whether they do not
		// correspond with any files associated with the MAS file.
		for (FileNode node : currentChildren) {
			if (!newFiles.contains(node.getBaseFile())) {
				// Move node to the spurious file node.
				appendNode(null, node);
			}
		}
		// CHECK is this always absolute path?
		String masdir = masNode.getBaseFile().getParent();
		// Check whether agent files need to be inserted.
		for (File agentFile : newFiles) {
			if (!agentFile.exists()) {
				// doesn't exist. Suggest to create
				String path = agentFile.getPath();
				File newFile = new File(path);
				if (!newFile.isAbsolute()) {
					// If relative, prepend MAS dir. #2926
					newFile = new File(FilenameUtils.concat(masdir,
							agentFile.getPath()));
				}
				try {
					proposeToCreate(this, newFile, Extension.GOAL);
					this.platform.parseGOALFile(newFile,
							mas.getKRInterface(newFile));
				} catch (GOALUserError ignore) {
					// this file does not really exist. #2692
					// We want to continue here to handle all other GOAL files,
					// even if user cancelled creation of one of these or IO
					// error happened.
				} catch (ParserException e) {
					// Even in case of serious parse errors we simply skip
					// the current agent file and continue with the next.
					// We want to process as many agent files as we can.
					continue;
				}
			}
			if (!currentFiles.contains(agentFile)) {
				GOALNode newNode = new GOALNode(agentFile);
				this.allFiles.add(newNode);
				// GOALNode newNode = insertGOALfile(agentFile.getAgentFile());
				appendNode(masNode, newNode);
				// also let the tree scroll to the newly added node
				this.fileTree.scrollPathToVisible(new TreePath(masNode
						.getPath()));
			}
		}

		refreshSpuriousList();

	}

	/**
	 * Something has changed in the files, and we need to check the position of
	 * the given GOALNode in the hierarchy.<br>
	 * For example, the node was spurious but now a MAS file has been opened
	 * that is using the .goal file.
	 *
	 * <p>
	 * This function also calls {@link EditManager#updateBreakpoints(File)},
	 *
	 *
	 * @param goalNode
	 *            is the node to be checked.
	 * @param askForNonExistentFiles
	 *            if we find a file that this .goal file refers to that does not
	 *            exist, we may propose to create it. Set this to true to do
	 *            such a proposal, or false to just ignore.
	 */
	private void refreshGOALNode(GOALNode goalNode,
			boolean askForNonExistentFiles) {
		// do nothing if the given node is not part of the tree (or null)
		// CHECK even in that case we CAN still check more...
		if (!this.rootNode.isNodeDescendant(goalNode)) {
			return;
		}

		AgentProgram goal = this.platform.getAgentProgram(goalNode
				.getBaseFile());
		if (goal == null) {
			return;
		}

		/*
		 * note, if we end up here the goal program might not be validated.
		 * #2944. but we can always try to show children (imports)
		 */
		try {
			List<File> childFiles = null;
			FileNode parent = (FileNode) goalNode.getParent();
			// Only create nodes for children of agent (.goal) file if the file
			// node
			// is located below a MAS node.
			if (parent instanceof MASNode) {
				childFiles = this.platform.getImportedFiles(goalNode
						.getBaseFile());
			} else if (parent instanceof NullNode) {
				// we do not save the parsed GOALPrograms of spurious agents
				// anywhere, so unless we want to parse the file twice we
				// cannot. CHECK this seems a bug, why can't we parse this???
				return;
			} else {
				// we know that parent != null, since the node is a child of
				// the root node, and is not the root node itself.
				throw new GOALBug("The parent of a GOALNode should be either " //$NON-NLS-1$
						+ "a MASNode or the NullNode (but is a " //$NON-NLS-1$
						+ parent.getClass() + ")"); //$NON-NLS-1$
			}

			// get the current children of the goal node, such that we do not
			// need to remove and add nodes referencing the same file.
			// CHECK why don't we use goalnode.isNodeChild() here?
			// and why don't we use the node iterator but use a counter?
			// CHECK this
			ArrayList<FileNode> oldChildren = new ArrayList<FileNode>(
					goalNode.getChildCount());
			for (int i = 0; i < goalNode.getChildCount(); i++) {
				oldChildren.add((FileNode) goalNode.getChildAt(i));
			}

			// Add files that are really new.
			List<File> newFiles = new ArrayList<File>();
			if (childFiles != null) {
				newFiles.addAll(childFiles);
			}
			for (FileNode alreadyThere : oldChildren) {
				newFiles.remove(alreadyThere.getBaseFile());
			}
			addChildren(newFiles, goalNode, askForNonExistentFiles);

			// determine files that have been removed and remove them
			for (FileNode oldChild : oldChildren) {
				if (!(childFiles.contains(oldChild.getBaseFile()))) {
					// NOTE:append(null,..) REMOVES a node.
					appendNode(null, oldChild);
				}
			}
		} finally {
			refreshSpuriousList();
		}
		EditManager.getInstance().updateBreakpoints(goalNode.getBaseFile());
	}

	/**
	 * add new childFiles to given goal node. We can ask the user if he wants to
	 * create the file if it does not exist already. We throw a GOALUserError if
	 * he does not want to create
	 * <p>
	 * (CHECK is this the right way to generate an exception? Is not wanting to
	 * create a file really an exception at all at this level?)
	 *
	 * @param childFiles
	 *            the files to be added
	 * @param goalnode
	 *            the node to insert the files in
	 * @param askForNonExistentFiles
	 *            true if user should be asked when generatign new files
	 * @return the list of new {@link FileNode}s.
	 */
	private ArrayList<FileNode> addChildren(List<File> childFiles,
			GOALNode goalnode, boolean askForNonExistentFiles) {
		ArrayList<FileNode> newChildren = new ArrayList<FileNode>();
		// finally add all the new children
		for (File file : childFiles) {
			if (!file.exists() && askForNonExistentFiles) {
				try {
					proposeToCreate(this, file, Extension.MODULES);
				} catch (GOALUserError ignore) {
				}
			}
			FileNode newNode = null;
			Extension ext = Extension.getFileExtension(file);
			if (ext == null) {
				throw new GOALBug("The sub-files of a GOALProgram should" //$NON-NLS-1$
						+ " always have a recognized extension."); //$NON-NLS-1$
			}
			switch (ext) {
			default:
			case GOAL:
			case MAS:
				break;
			case MODULES:
				newNode = new ModulesNode(file);
				break;
			case PROLOG:
				newNode = new PrologNode(file);
				break;
			}
			if (newNode != null) {
				this.allFiles.add(newNode);
				// There is no need to add the same file twice
				if (!newChildren.contains(newNode)) {
					newChildren.add(newNode);
					// efficiency (somewhat): there is no need to add a node
					// that
					// is already a child
					if (!goalnode.isNodeChild(newNode)) {
						appendNode(goalnode, newNode);
					}
				}
			}
		}
		return newChildren;
	}

	/**
	 * propose user to create new file as it does not exist now. Throws
	 * GOALUserError if user cancels the proposal or if creation of file failed.
	 *
	 * It's a bit weird to have static function in FilePanel, but also it would
	 * be weird to put a GUI function in IOManager. CHECK
	 *
	 * @param parent
	 *            is the GUI parent for this panel, used for centering the
	 *            panel.
	 * @param newFile
	 *            is {@link File} to be created. Filename should have the
	 *            extension.
	 * @param extension
	 *            is the type of extension to be made, used to pick up the
	 *            appropriate template for the file.
	 */
	public static void proposeToCreate(Container parent, File newFile,
			Extension extension) throws GOALUserError {
		try {
			int selection = JOptionPane.showConfirmDialog(parent, "The file " //$NON-NLS-1$
					+ newFile + " does not exist. Create it?", //$NON-NLS-1$
					"Create new file?", JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
			if (selection == JOptionPane.NO_OPTION) {
				throw new GOALUserError("File " + newFile //$NON-NLS-1$
						+ " does not exist; creation cancelled"); //$NON-NLS-1$
			}
			PlatformManager.createfile(newFile, extension);
		} catch (IOException e) {
			throw new GOALUserError("Cannot create file " + newFile, e); //$NON-NLS-1$
		}
	}

	/**
	 * Refreshes the null-node, which is the parent of the spurious goal files.
	 * Removes any goal-files that do not exist or are used in some of the .mas
	 * files. Also removes duplicate spurious files.
	 */
	public void refreshSpuriousList() {

		List<FileNode> nullNodes = getChildrenOf(this.nullNode);
		for (FileNode nnode : nullNodes) {
			// remove non-existing children from the null file node
			if (!nnode.getBaseFile().exists()) {
				// CHECK: removing the node from the allFiles list
				// as well seems to have the effect that the node
				// remains in the tree.
				removeNodeFromTree(nnode);
				continue;
			}
			// also remove files also existing somewhere else
			List<FileNode> nodes = this.allFiles.getAll(nnode.getBaseFile());
			if (nodes != null && nodes.size() > 1) {
				this.allFiles.remove(nnode);
				removeNodeFromTree(nnode);
			}
		}
		ActionFactory.broadcastStateChange(this.ideState);
	}

	/**
	 * Gets the {@link FileNode}s that are a child of the given {@link FileNode}
	 * .
	 *
	 * @param fileNode
	 *            The {@link FileNode} to get the children of.
	 * @return The list of {@link FileNode}s of which the given {@link FileNode}
	 *         is the parent.
	 */
	private List<FileNode> getChildrenOf(FileNode fileNode) {
		List<FileNode> children = new ArrayList<FileNode>();
		for (FileNode node : this.allFiles.allValues()) {
			if (node.getParent() == fileNode) {
				children.add(node);
			}
		}
		return children;
	}

	/**
	 * Updates the tree model so that the children of the given MAS node
	 * correspond to the agent files described in the MAS file. Any .goal files
	 * that are children of the given MAS node but are not referenced in the MAS
	 * file will be moved to the null file node.
	 *
	 * @param masFile
	 *            The mas file of which the children should be refreshed.
	 * @param showLoadError
	 *            If this is <code>false</code>, any GOALException thrown when
	 *            getting the agent file names will be ignored. If
	 *            <code>true</code>, the user will be notified of them.<br>
	 *            Use <code>false</code> when this is called after saving, as
	 *            the message should have already been displayed.
	 */
	public void refreshMASFile(File masFile) {
		MASNode node = null;
		List<FileNode> nodes = this.allFiles.getAll(masFile);
		if (nodes != null && nodes.size() > 0) {
			node = (MASNode) nodes.get(0);
		}
		refreshMASNode(node);
	}

	/**
	 * Updates the tree model so that the children of the given .goal file node
	 * correspond to the files described in the agent file. Any files that are
	 * children of the agent file but are not referenced in the agent file will
	 * be moved to the null file node.
	 *
	 * @param goalFile
	 *            The goal/agent file of which the children should be refreshed.
	 */
	public void refreshGOALFile(File goalFile) {
		if (goalFile == null) {
			return; // quick solution to #2638
		}
		boolean askForNonExistentFiles = true;
		for (FileNode node : this.allFiles.getAll(goalFile)) {
			refreshGOALNode((GOALNode) node, askForNonExistentFiles);
			// only ask the user once if a file should be created.
			askForNonExistentFiles = false;
		}
	}

	/**
	 * After the file contents of given file were saved, Updates the tree model
	 * so that the children of the given goal node correspond to the goal files
	 * described in the agent file. Any files that are children of the agent
	 * file but are not referenced to in the agent file will be moved to the
	 * null file node.
	 *
	 * @param mod2gFile
	 *            the file that was changed
	 */
	public void refreshMod2gFile(File mod2gFile) {
		for (FileNode node : this.allFiles.getAll(mod2gFile)) {
			refreshModuleNode((ModulesNode) node);
		}
	}

	/**
	 * A Module node needs refreshing. This is done by refreshing its parent
	 * GOAL node.
	 *
	 * @param node
	 *            is a {@link ModulesNode} in the Files tree.
	 */
	private void refreshModuleNode(ModulesNode node) {
		if (!this.rootNode.isNodeDescendant(node)) {
			// apparently you can edit files that are not in the files
			// tree. Maybe for example when exporting an agent database?
			return;
		}
		TreeNode parent = node.getParent();
		if (parent instanceof ModulesNode) {
			refreshModuleNode((ModulesNode) parent);
		} else if (parent instanceof GOALNode) {
			refreshGOALNode((GOALNode) parent, false);
		}
		// ignore other unknown parents. #2960

	}

	/**
	 * A Prolog node needs refreshing. This is done by refreshing its parent
	 * .goal file node.
	 *
	 * @param node
	 *            is a {@link PrologNode} in the Files tree.
	 */

	private void refreshPrologNode(PrologNode node) {
		if (!this.rootNode.isNodeDescendant(node)) {
			// apparently you can edit files that are not in the files
			// tree. Maybe for example when exporting an agent database?
			return;
		}
		TreeNode parent = node.getParent();
		if (parent instanceof ModulesNode) {
			refreshModuleNode((ModulesNode) parent);
		} else if (parent instanceof GOALNode) {
			refreshGOALNode((GOALNode) parent, false);
		}
		// ignore other unknown parents. #2960
	}

	/**
	 * After the file contents of given file were saved, Updates the tree model
	 * so that the children of the given goal node correspond to the goal files
	 * described in the agent file. Any files that are children of the agent
	 * file but are not referenced to in the agent file will be moved to the
	 * null file node.
	 *
	 * @param prologFile
	 *            the file that was changed
	 */
	public void refreshPrologFile(File prologFile) {
		for (FileNode node : this.allFiles.getAll(prologFile)) {
			refreshPrologNode((PrologNode) node);
		}

	}

	/**
	 * Renames a file to a new name. THe new name will be asked from the user.
	 * If the selected target file exists we overwrite the existing file with
	 * the given (after user's confirmation).
	 *
	 * @param oldFile
	 *            is the file to be renamed.
	 * @throws GOALException
	 * @throws ParserException
	 */
	public void rename(File oldFile) throws GOALException, ParserException {
		// step 1. ask new filename.
		// use the general getExtension to support unknown extensions properly.
		String extension = FilenameUtils.getExtension(oldFile.getName());
		String oldfilename = FilenameUtils.removeExtension(oldFile
				.getAbsolutePath());
		File newFile = null;
		try {
			newFile = SimpleIDE.askFile(this.mainpanel, false,
					"Save as", //$NON-NLS-1$
					JFileChooser.FILES_ONLY, extension, oldfilename,
					PMPreferences.getAgentBrowsePath(), true);
		} catch (GOALCommandCancelledException ignore) {
			return;
		}
		if (PMPreferences.getRememberLastUsedAgentDir()) {
			PMPreferences.setAgentBrowsePath(newFile.getParent());
		}
		Extension ext = Extension.getFileExtension(newFile);
		// step 1b. disallow renaming to an existing project present in the IDE
		if (ext == Extension.MAS && containsFile(newFile)) {
			throw new GOALUserError("Cannot rename a MAS project to " //$NON-NLS-1$
					+ "another MAS project present in the IDE."); //$NON-NLS-1$
		}

		// this is a bit tricky, as we should try to keep this functionality
		// separate
		// from the editor panel functionality...
		// step 2. close editor(s)
		boolean isEditingOldFile = EditManager.getInstance().isOpenEditor(
				oldFile);
		// mainPanel.getEditPanel().saveAs(newFile.getAbsolutePath());
		// edit panel close old editor and open new one.
		if (isEditingOldFile) {
			EditManager.getInstance().close(oldFile);
		}
		boolean isEditingNewFile = EditManager.getInstance().isOpenEditor(
				newFile);
		// mainPanel.getEditPanel().saveAs(newFile.getAbsolutePath());
		// edit panel close old editor and open new one.
		if (isEditingNewFile) {
			EditManager.getInstance().close(newFile);
		}

		// step 3. make copy
		try {
			FileInputStream input = new FileInputStream(oldFile);
			FileOutputStream output = new FileOutputStream(newFile);
			IOUtils.copy(input, output);
			input.close();
			output.close();
		} catch (IOException e) {
			throw new GOALUserError(
					"Cannot copy " + oldFile + " to " + newFile, e); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// step 4. reload some stuff
		// HACK, #1061
		switch (ext) {
		case GOAL:
			KRInterface language = this.platform.getAgentProgram(oldFile)
					.getKRInterface();
			this.platform.parseGOALFile(newFile, language);
			this.platform.removeParsedProgram(oldFile);
			break;
		case MAS:
			// add new file, remove old file
			this.platform.parseMASFile(newFile);
			this.platform.removeParsedProgram(oldFile);
			break;
		default:
			// other files are not parsed
			break;
		}

		// step 5. fix file panel.
		boolean oldFileInUse = !handleFileRename(oldFile, newFile);

		// step 6. refresh nodes, but only if any of the nodes reference the
		// new file
		if (containsFile(newFile)) {
			if (ext == Extension.MAS) {
				// refresh the MAS file node. placed here since it is
				// not part of the #1061 hack
				refreshMASFile(newFile);
			} else if (ext == Extension.GOAL) {
				refreshGOALFile(newFile);
			}
		}

		// step 7. restore editing configuration if possible.
		if (isEditingOldFile && oldFileInUse) {
			EditManager.getInstance().editFile(oldFile);
		}
		if (isEditingOldFile || isEditingNewFile) {
			EditManager.getInstance().editFile(newFile);
		}

		// step 8. delete old file if not used anymore.
		if (!oldFileInUse) {
			try {
				oldFile.delete();
			} catch (SecurityException e) {
				new Warning(
						Resources
								.get(WarningStrings.FAILED_REMOVE_AFTER_RENAME),
						e);
			}
		}
	}

	/**
	 * Handle the renaming of a file after the file was renamed on the
	 * filesystem.
	 *
	 * @param oldfile
	 *            The file as it was on the filesystem before it was renamed
	 * @param newFile
	 *            The file after it was renamed, as it is now on the filesystem.
	 * @return true if there is a MAS still using the old filename after the
	 *         rename. Else false
	 */
	public boolean handleFileRename(File oldfile, File newFile)
			throws GOALUserError {
		boolean oldFileStillUsed = false; // maybe we find out otherwise later.

		if (this.allFiles.containsKey(oldfile)) {

			// allow renaming to a filename that already exists in the
			// tree (trac #1651)
			/*
			 * if (this.allFiles.containsKey(newFile)) { throw new
			 * GOALUserError("Cannot set the name of a file to " +
			 * "something that already exists."); }
			 */

			List<FileNode> similarNodes = this.allFiles.getAll(oldfile);
			List<FileNode> affectedNodes;
			if (similarNodes == null) {
				return oldFileStillUsed;
			}
			if (similarNodes.size() == 1) {
				// only one with the same name? Asking the user will only be
				// annoying.
				this.allFiles.removeAll(oldfile);
				affectedNodes = similarNodes;
			} else {
				// ask the user which of the occurrences should be moved.
				affectedNodes = filterFiles(similarNodes);
				/*
				 * same number of affected nodes as number of nodes with the
				 * same file? remove all old ones in one step, as well as the
				 * map entry. Otherwise just remove the affected nodes from the
				 * list of similar nodes.
				 */
				if (similarNodes.size() == affectedNodes.size()) {
					this.allFiles.removeAll(oldfile);
				} else {
					for (FileNode affectedNode : affectedNodes) {
						this.allFiles.remove(affectedNode);
					}
					oldFileStillUsed = true;
				}
			}
			for (FileNode n : affectedNodes) {
				n.setBaseFile(newFile);
				this.treeModel.nodeChanged(n);
				this.allFiles.add(n);
			}
			refreshSpuriousList();
			showRenameWarning(oldfile);
		}
		return oldFileStillUsed;
		// ignore command if given file is not present
	}

	/**
	 * show warning after renaming or moving file f
	 *
	 * @param f
	 *            is file that was renamed
	 */
	private void showRenameWarning(File f) {
		switch (Extension.getFileExtension(f)) {
		case GOAL:
			JOptionPane.showMessageDialog(this, "Don't forget to edit\n" //$NON-NLS-1$
					+ " the MAS file manually\n" //$NON-NLS-1$
					+ " to match your new file name."); //$NON-NLS-1$
			break;
		case MODULES:
		case PROLOG:
			JOptionPane.showMessageDialog(this, "Don't forget to edit\n" //$NON-NLS-1$
					+ " the GOAL file manually\n" //$NON-NLS-1$
					+ " to match your new file name."); //$NON-NLS-1$
			break;
		case MAS:
		default:
			break;
		}

	}

	/**
	 * Helper method for {@link #handleFileRename(File, File)}. Asks the user
	 * which of the given list of file references should be renamed. Returns a
	 * new list of nodes that the user has selected. The given list is not
	 * altered.
	 */
	private List<FileNode> filterFiles(List<FileNode> nodes) {

		ArrayList<FileNode> filteredFiles = new ArrayList<FileNode>();

		// show requester which files to change.
		// we can not dump an array of checkboxes into the showOptionDialog
		// because it will show them next to each oter instead of below each
		// other.
		// and because it does not show the OK and Cancel buttons. So we use
		// ConfirmDialog isntead
		JPanel choicespanel = new JPanel();
		choicespanel.setLayout(new BoxLayout(choicespanel, BoxLayout.Y_AXIS));
		choicespanel.add(new JLabel(
				"There are multiple mas's using the renamed file.\n" //$NON-NLS-1$
						+ "Please select which ones have to be renamed")); //$NON-NLS-1$
		ArrayList<JCheckBox> choices = new ArrayList<JCheckBox>();
		for (FileNode n : nodes) {
			JCheckBox checkbox = new JCheckBox("" //$NON-NLS-1$
					+ ((FileNode) n.getParent()).getFilename());
			choices.add(checkbox);
			choicespanel.add(checkbox);
		}

		int choice = JOptionPane.showConfirmDialog(this, choicespanel,
				"Select files to rename", JOptionPane.OK_CANCEL_OPTION, //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE);
		if (choice == JOptionPane.CANCEL_OPTION) {
			return null;
		}
		// and finally copy the requested filenames to array
		for (JCheckBox c : choices) {
			if (c.isSelected()) {
				for (FileNode n : nodes) {
					if (c.getText().equals(
							((FileNode) n.getParent()).getFilename())) {
						filteredFiles.add(n);
						break;
					}
				}
			}
		}

		return filteredFiles;
	}

	/**
	 * Removes a node from this file panel, but only if it actually can be
	 * removed. All child {@link FileNode} will be removed as well.<br>
	 * This should only be called as a result of the IDE 'remove' command.
	 *
	 * @param fn
	 *            The file node to remove.
	 */
	public void removeNode(FileNode fn) {

		// make sure to ignore removing the root/null/project nodes
		// this should not be possible (the command should be disabled for
		// those), but check anyway.
		switch (fn.getType()) {
		case ROOT: // covers root and projects node
		case NULLFILE: // the null-node
			return;
		default:
			break;
		}

		// do not move any of the children (or the node itself) to the
		// null-node, since the user explicitly wanted to remove them.
		for (FileNode child : getChildrenOf(fn)) {
			this.allFiles.remove(child);
			removeNodeFromTree(child);
		}
		this.allFiles.remove(fn);
		removeNodeFromTree(fn);
		// update the list of other files, just to be sure
		refreshSpuriousList();
	}

	/**
	 * Gets the parent {@link FileNode} of the given node that is a
	 * {@link MASNode}. May also be the node itself. Returns null when there is
	 * no parent {@link MASNode}.
	 */
	public FileNode getParentMASNode(FileNode node) {
		while (node.getType() != NodeType.MASFILE) {
			node = (FileNode) node.getParent();
			// handle cases where the selected node is
			// the root, an other file or the root of the other files.
			if (node == null || node.getType() == NodeType.NULLFILE
					|| node.getType() == NodeType.ROOT) {
				return null;
			}
		}
		return node;
	}

	/**
	 * Returns the list of MAS files displayed in this {@link FilePanel}.
	 *
	 * @return A list of all MAS files displayed in the panel.
	 */
	public List<File> getMASFiles() {
		List<File> masPaths = new ArrayList<File>();
		for (FileNode node : getChildrenOf(this.projectsNode)) {
			masPaths.add(node.getBaseFile());
		}
		return masPaths;
	}

	/**
	 * @return The paths to the GOAL-files under the 'Other files' node.
	 */
	public List<File> getOtherPaths() {
		ArrayList<File> goalPaths = new ArrayList<File>();
		for (FileNode goalfile : getChildrenOf(this.nullNode)) {
			goalPaths.add(goalfile.getBaseFile());
		}
		return goalPaths;
	}

	/**
	 * Deletes the given {@link FileNode}. Will ask the user for permission
	 * first however.
	 *
	 * @param node
	 *            The node to delete.
	 * @return {@code true} iff the file was deleted. CHECK nobody cares about
	 *         return value
	 * @throws GOALIncompleteGUIUsageException
	 *             If the user wanted to delete a MAS project while there are
	 *             still child-files present.
	 */
	public boolean deleteNodeFile(FileNode node)
			throws GOALIncompleteGUIUsageException {
		// first check what's possible. See trac 675
		switch (node.getType()) {
		case GOALFILE:
		case MODFILE:
		case PLFILE:
		case TXTFILE:
			break;
		case MASFILE:
			for (FileNode child : getChildrenOf(node)) {
				if (child.getBaseFile().exists()) {
					throw new GOALIncompleteGUIUsageException(
							"A MAS file can only be deleted after removing " //$NON-NLS-1$
									+ "all related project files."); //$NON-NLS-1$
				}
			}
			break;
		default:
			throw new GOALBug("Attempt to delete filenode of type " + node); //$NON-NLS-1$
		}
		// if we get here, the file can be deleted.
		int selection = JOptionPane.showConfirmDialog(this,
				"This will delete the file " + node.getFilename() //$NON-NLS-1$
						+ " from the file system. Please confirm.", //$NON-NLS-1$
				"Pleas Confirm", JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
		if (selection == JOptionPane.YES_OPTION) {
			File f = node.getBaseFile();
			if (!f.delete()) {
				f.deleteOnExit();
			}
			// remove all nodes referencing the deleted file
			List<FileNode> fNodes = new ArrayList<FileNode>(
					this.allFiles.getAll(node.getBaseFile()));
			for (FileNode fNode : fNodes) {
				removeNode(fNode);
			}
			return true;
		}
		return false;
	}

	/**
	 * This maps each filename to a list of nodes (in the FilesPanel) that links
	 * to that file. It is a wrapper for a Map&lt;File,
	 * List&lt;FileNode&gt;&gt;, to ensure that the keys are always accessed by
	 * absolute files. The base files of file nodes are generally already
	 * absolute, but we need to be certain. <br>
	 * This also groups the methods accessing the actual map in a more logical
	 * way.
	 *
	 * @author N.Kraayenbrink
	 */
	private static class FileNodeMap {
		/**
		 * The actual map between files and file nodes.
		 */
		private final Map<File, List<FileNode>> nodeMap;

		/**
		 * Creates a new, empty map between {@link File}s and {@link FileNode}s.
		 */
		public FileNodeMap() {
			this.nodeMap = new HashMap<File, List<FileNode>>();
		}

		/**
		 * Adds a single {@link FileNode} to this map. The key will be the base
		 * file of the given node.
		 */
		public void add(FileNode node) {
			File absFile = node.getBaseFile().getAbsoluteFile();
			if (!this.nodeMap.containsKey(absFile)) {
				this.nodeMap.put(absFile, new ArrayList<FileNode>(1));
			}
			this.nodeMap.get(absFile).add(node);
		}

		/**
		 * Returns all {@link FileNode}s associated with the given {@link File}.
		 * Only the nodes directly referring to the file are returned, not the
		 * MAS files that may indirectly contain the referred file.
		 *
		 * @return a copy of the associated files. Returning a copy allows you
		 *         to iterate through the list and modifying the nodes involved
		 *         while modification may change the list order.
		 */
		public List<FileNode> getAll(File file) {
			List<FileNode> nodes = this.nodeMap.get(file.getAbsoluteFile());
			List<FileNode> list = new ArrayList<FileNode>();
			if (nodes != null) {
				list.addAll(nodes);
			}
			return list;
		}

		/**
		 * @return {@code true} iff this map contains at least one
		 *         {@link FileNode} for the given {@link File}.
		 */
		public boolean containsKey(File file) {
			return this.nodeMap.containsKey(file.getAbsoluteFile());
		}

		/**
		 * Removes a FileNode from this map, and only that exact node. There may
		 * still be other {@link FileNode}s in the map, but they are no the same
		 * object.<br>
		 * If this method removes the last FileNode with a certain base file,
		 * the entry is removed from the map.
		 */
		public void remove(FileNode fileNode) {
			File absFile = fileNode.getBaseFile().getAbsoluteFile();
			if (!this.nodeMap.containsKey(absFile)) {
				return;
			}
			List<FileNode> allNodes = this.nodeMap.get(absFile);
			// FileNode.equals only checks base file, but we only want to
			// remove the given instance. So we need to iterate over the list
			// to find the specific reference and remove that one.
			for (int i = 0; i < allNodes.size(); i++) {
				if (allNodes.get(i) == fileNode) {
					allNodes.remove(i);
					// only one instance of the same node should ever be present
					break;
				}
			}
			// remove the entry if this was the last file node for that file.
			if (allNodes.isEmpty()) {
				this.nodeMap.remove(absFile);
			}
		}

		/**
		 * Removes all {@link FileNode}s linked with the given file.
		 *
		 * @param file
		 *            The {@link File} to remove from this map.
		 * @return The list of {@link FileNode}s previously associated with the
		 *         given {@link File}, or null if there was no such mapping.
		 */
		public List<FileNode> removeAll(File file) {
			return this.nodeMap.remove(file.getAbsoluteFile());
		}

		/**
		 * @return An iteration over all {@link FileNode}s contained within this
		 *         map. {@link FileNode}s with the same base file will be
		 *         grouped together.
		 */
		public Iterable<FileNode> allValues() {
			return new IterableStitcher<FileNode>(this.nodeMap.values());
		}

		@Override
		public String toString() {
			return this.nodeMap.toString();
		}
	}
}

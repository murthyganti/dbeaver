/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ui.data.managers.stream;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.controls.imageview.ImageEditor;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * CONTENT text editor
 */
public class ImageEditorPart extends EditorPart implements IResourceChangeListener {

    private static final Log log = Log.getLog(ImageEditorPart.class);

    private ImageEditor imageViewer;
    private boolean contentValid;

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    @Override
    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        imageViewer = new ImageEditor(parent, SWT.NONE);

        loadImage();
    }

    private void loadImage() {
        if (imageViewer == null || imageViewer.isDisposed()) {
            return;
        }
        if (getEditorInput() instanceof IPathEditorInput) {
            try {
                final IPath absolutePath = ((IPathEditorInput)getEditorInput()).getPath();
                File localFile = absolutePath.toFile();
                if (localFile.exists()) {
                    try (InputStream inputStream = new FileInputStream(localFile)) {
                        contentValid = imageViewer.loadImage(inputStream);
                        imageViewer.update();
                    }
                }
            }
            catch (Exception e) {
                log.error("Can't load image contents", e);
            }
        }
    }

    @Override
    public void setFocus() {
        imageViewer.setFocus();
    }

    @Override
    public String getTitle()
    {
        return "Image";
    }

    @Override
    public Image getTitleImage()
    {
        return DBeaverIcons.getImage(DBIcon.TYPE_IMAGE);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IResourceDelta delta = event.getDelta();
        if (delta == null) {
            return;
        }
        IEditorInput input = getEditorInput();
        IPath localPath = null;
        if (input instanceof IPathEditorInput) {
            localPath = ((IPathEditorInput) input).getPath();
        }
        if (localPath == null) {
            return;
        }
        localPath = ContentUtils.convertPathToWorkspacePath(localPath);
        delta = delta.findMember(localPath);
        if (delta == null) {
            return;
        }
        if (delta.getKind() == IResourceDelta.CHANGED) {
            // Refresh editor
            DBeaverUI.asyncExec(new Runnable() {
                @Override
                public void run() {
                    loadImage();
                }
            });
        }
    }

}
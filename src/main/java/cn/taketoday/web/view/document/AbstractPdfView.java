/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2022 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.web.view.document;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

import cn.taketoday.web.RequestContext;
import cn.taketoday.web.view.AbstractView;

/**
 * Abstract superclass for PDF views. Application-specific view classes
 * will extend this class. The view will be held in the subclass itself,
 * not in a template.
 *
 * <p>This view implementation uses Bruno Lowagie's
 * <a href="https://www.lowagie.com/iText">iText</a> API.
 * Known to work with the original iText 2.1.7 as well as its fork
 * <a href="https://github.com/LibrePDF/OpenPDF">OpenPDF</a>.
 * <b>We strongly recommend OpenPDF since it is actively maintained
 * and fixes an important vulnerability for untrusted PDF content.</b>
 *
 * <p>Note: Internet Explorer requires a ".pdf" extension, as it doesn't
 * always respect the declared content type.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @see AbstractPdfStamperView
 */
public abstract class AbstractPdfView extends AbstractView {

  /**
   * This constructor sets the appropriate content type "application/pdf".
   * Note that IE won't take much notice of this, but there's not a lot we
   * can do about this. Generated documents should have a ".pdf" extension.
   */
  public AbstractPdfView() {
    setContentType("application/pdf");
  }

  @Override
  protected boolean generatesDownloadContent() {
    return true;
  }

  @Override
  protected final void renderMergedOutputModel(
          Map<String, Object> model, RequestContext context) throws Exception {

    // IE workaround: write into byte array first.
    ByteArrayOutputStream baos = createTemporaryOutputStream();

    // Apply preferences and build metadata.
    Document document = newDocument();
    PdfWriter writer = newWriter(document, baos);
    prepareWriter(model, writer, context);
    buildPdfMetadata(model, document, context);

    // Build PDF document.
    document.open();
    buildPdfDocument(model, document, writer, context);
    document.close();

    // Flush to HTTP response.
    writeToResponse(context, baos);
  }

  /**
   * Create a new document to hold the PDF contents.
   * <p>By default returns an A4 document, but the subclass can specify any
   * Document, possibly parameterized via bean properties defined on the View.
   *
   * @return the newly created iText Document instance
   * @see com.lowagie.text.Document#Document(com.lowagie.text.Rectangle)
   */
  protected Document newDocument() {
    return new Document(PageSize.A4);
  }

  /**
   * Create a new PdfWriter for the given iText Document.
   *
   * @param document the iText Document to create a writer for
   * @param os the OutputStream to write to
   * @return the PdfWriter instance to use
   * @throws DocumentException if thrown during writer creation
   */
  protected PdfWriter newWriter(Document document, OutputStream os) throws DocumentException {
    return PdfWriter.getInstance(document, os);
  }

  /**
   * Prepare the given PdfWriter. Called before building the PDF document,
   * that is, before the call to {@code Document.open()}.
   * <p>Useful for registering a page event listener, for example.
   * The default implementation sets the viewer preferences as returned
   * by this class's {@code getViewerPreferences()} method.
   *
   * @param model the model, in case meta information must be populated from it
   * @param writer the PdfWriter to prepare
   * @param request in case we need locale etc. Shouldn't look at attributes.
   * @throws DocumentException if thrown during writer preparation
   * @see com.lowagie.text.Document#open()
   * @see com.lowagie.text.pdf.PdfWriter#setPageEvent
   * @see com.lowagie.text.pdf.PdfWriter#setViewerPreferences
   * @see #getViewerPreferences()
   */
  protected void prepareWriter(Map<String, Object> model, PdfWriter writer, RequestContext request)
          throws DocumentException {

    writer.setViewerPreferences(getViewerPreferences());
  }

  /**
   * Return the viewer preferences for the PDF file.
   * <p>By default returns {@code AllowPrinting} and
   * {@code PageLayoutSinglePage}, but can be subclassed.
   * The subclass can either have fixed preferences or retrieve
   * them from bean properties defined on the View.
   *
   * @return an int containing the bits information against PdfWriter definitions
   * @see com.lowagie.text.pdf.PdfWriter#AllowPrinting
   * @see com.lowagie.text.pdf.PdfWriter#PageLayoutSinglePage
   */
  protected int getViewerPreferences() {
    return PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage;
  }

  /**
   * Populate the iText Document's meta fields (author, title, etc.).
   * <br>Default is an empty implementation. Subclasses may override this method
   * to add meta fields such as title, subject, author, creator, keywords, etc.
   * This method is called after assigning a PdfWriter to the Document and
   * before calling {@code document.open()}.
   *
   * @param model the model, in case meta information must be populated from it
   * @param document the iText document being populated
   * @param request in case we need locale etc. Shouldn't look at attributes.
   * @see com.lowagie.text.Document#addTitle
   * @see com.lowagie.text.Document#addSubject
   * @see com.lowagie.text.Document#addKeywords
   * @see com.lowagie.text.Document#addAuthor
   * @see com.lowagie.text.Document#addCreator
   * @see com.lowagie.text.Document#addProducer
   * @see com.lowagie.text.Document#addCreationDate
   * @see com.lowagie.text.Document#addHeader
   */
  protected void buildPdfMetadata(
          Map<String, Object> model, Document document, RequestContext request) { }

  /**
   * Subclasses must implement this method to build an iText PDF document,
   * given the model. Called between {@code Document.open()} and
   * {@code Document.close()} calls.
   * <p>Note that the passed-in HTTP response is just supposed to be used
   * for setting cookies or other HTTP headers. The built PDF document itself
   * will automatically get written to the response after this method returns.
   *
   * @param model the model Map
   * @param document the iText Document to add elements to
   * @param writer the PdfWriter to use
   * @param context in case we need locale etc. Shouldn't look at attributes.
   * @throws Exception any exception that occurred during document building
   * @see com.lowagie.text.Document#open()
   * @see com.lowagie.text.Document#close()
   */
  protected abstract void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
                                           RequestContext context) throws Exception;

}

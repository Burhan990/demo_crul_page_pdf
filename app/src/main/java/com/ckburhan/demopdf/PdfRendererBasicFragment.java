package com.ckburhan.demopdf;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ckburhan.demopdf.pageCrul.PageCurlView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PdfRendererBasicFragment extends Fragment implements View.OnClickListener {


    /**
     * Key string for saving the state of current page index.
     */
    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

    /**
     * File descriptor of the PDF.
     */
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * {@link android.graphics.pdf.PdfRenderer} to render the PDF.
     */
    private PdfRenderer mPdfRenderer;

    /**
     * Page that is currently shown on the screen.
     */
    private PdfRenderer.Page mCurrentPage;
    private PdfRenderer.Page mCurrentPage2;

    /**
     * {@link android.widget.ImageView} that shows a PDF page as a {@link android.graphics.Bitmap}
     */
    private ImageView mImageView;

    /**
     * {@link android.widget.Button} to move to the previous page.
     */
    private Button mButtonPrevious;

    /**
     * {@link android.widget.Button} to move to the next page.
     */
    private Button mButtonNext;
    int index = 0;
    public PdfRendererBasicFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pdf_renderer_basic, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retain view references.
        mImageView = (ImageView) view.findViewById(R.id.image);
        mButtonPrevious = (Button) view.findViewById(R.id.previous);
        mButtonNext = (Button) view.findViewById(R.id.next);
        pageCurlView = (PageCurlView)view.findViewById(R.id.pageCrulViewId);
        // Bind events.
        mButtonPrevious.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        // Show the first page by default.

        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        if (null != savedInstanceState) {
            index = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
        }
        openRenderer(requireContext());

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mCurrentPage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                outState.putInt(STATE_CURRENT_PAGE_INDEX, mCurrentPage.getIndex());
            }
        }
    }

    /**
     * Sets up a {@link PdfRenderer} and related resources.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context){
        // In this sample, we read a PDF from the assets directory.
        try {
            mFileDescriptor = context.getAssets().openFd("sample.pdf").getParcelFileDescriptor();
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
            showPage(index);
        } catch (IOException e) {
            Log.d("TAG", "openRenderer: "+e.getMessage());
            Toast.makeText(requireActivity(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // This is the PdfRenderer we use to render the PDF.

    }

    /**
     * Closes the {@link PdfRenderer} and related resources.
     *
     * @throws IOException When the PDF file cannot be closed.
     */
    private void closeRenderer() throws IOException {
        if (null != mCurrentPage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCurrentPage.close();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPdfRenderer.close();
        }
        mFileDescriptor.close();
    }

    /**
     * Shows the specified page of PDF to the screen.
     *
     * @param index The page index.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPage(int index) {
        List <Bitmap> images=new ArrayList<>();

        for (int i=0;i<mPdfRenderer.getPageCount();i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mPdfRenderer.getPageCount() <= i) {
                    return;
                }
            }
            // Make sure to close the current page before opening another one.
            if (null != mCurrentPage) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mCurrentPage.close();
                }
            }
            // Use `openPage` to open a specific page in PDF.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCurrentPage = mPdfRenderer.openPage(i);
            }
            // Important: the destination bitmap must be ARGB (not RGB).
            Bitmap bitmap = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                        Bitmap.Config.ARGB_8888);
            }
            // Here, we render the page onto the Bitmap.
            // To render a portion of the page, use the second and third parameter. Pass nulls to get
            // the default result.
            // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            }

            images.add(bitmap);
        }

        if (images!=null) {
            pageCurlView.setCurlView(images);
            pageCurlView.setCurlSpeed(200);
        }else {
            Log.d("TAG", "showPage: "+null);
        }
        // We are ready to show the Bitmap to user.
       // mImageView.setImageBitmap(bitmap);
        //updateUi();
    }
    PageCurlView pageCurlView;
    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    private void updateUi() {
        int index = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            index = mCurrentPage.getIndex();
        }
        int pageCount = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            pageCount = mPdfRenderer.getPageCount();
        }
        mButtonPrevious.setEnabled(0 != index);
        mButtonNext.setEnabled(index + 1 < pageCount);
       // getActivity().setTitle(getString(R.string.app_name_with_index, index + 1, pageCount));
    }

    /**
     * Gets the number of pages in the PDF. This method is marked as public for testing.
     *
     * @return The number of pages.
     */
    public int getPageCount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mPdfRenderer.getPageCount();
        }
        return 0;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previous: {
                // Move to the previous page
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    showPage(mCurrentPage.getIndex() - 1);
                }
                break;
            }
            case R.id.next: {
                // Move to the next page
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    showPage(mCurrentPage.getIndex() + 1);
                }
                break;
            }
        }
    }

}
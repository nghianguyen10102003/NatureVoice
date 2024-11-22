package vn.edu.usth.naturevoice;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link HomeFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomeFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment HomeFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static HomeFragment newInstance(String param1, String param2) {
//        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    private LinearLayout linearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_home, container, false);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        linearLayout = view.findViewById(R.id.image_container);
        refreshImages();
        return view;
    }
    private void refreshImages() {
        linearLayout.removeAllViews();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyApp", getActivity().MODE_PRIVATE);
        String savedIcons = sharedPreferences.getString("SELECTED_ICONS", "");

        if (!savedIcons.isEmpty()) {
            String[] icons = savedIcons.split(",");
            for (String icon : icons) {
                addImageView(icon);
            }
        }
    }

    private void addImageView(String selectIcon) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                200,
                200
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        switch (selectIcon) {
            case "i1_p1":
                imageView.setImageResource(R.drawable.cay1chau1);
                break;
            case "i1_p2":
                imageView.setImageResource(R.drawable.cay1chau2);
                break;
            case "i2_p1":
                imageView.setImageResource(R.drawable.cay2);
                break;
            case "i2_p2":
                imageView.setImageResource(R.drawable.chau2);
                break;
            default:
                break;
        }

        linearLayout.addView(imageView);
    }

}
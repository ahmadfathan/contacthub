package id.my.hubkontak.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import id.my.hubkontak.DetailContactActivity;
import id.my.hubkontak.R;
import id.my.hubkontak.WebviewActivity;
import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;
import id.my.hubkontak.utils.db.ModelContactSave;

import static id.my.hubkontak.utils.SessionManager.KEY_FOTO;
import static id.my.hubkontak.utils.SessionManager.KEY_USER_ID;
import static id.my.hubkontak.utils.SharedPref.KEY_CS_TOPUP;
import static id.my.hubkontak.utils.SharedPref.KEY_MESSAGE_TOPUP;

public class ContactSaveFragment extends Fragment {

    private static final String TAG = ContactSaveFragment.class.getSimpleName();
    private ModelContactSave modelContactSave;
    private EditText edtCari;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutError;
    private ListView listContact;
    private List<String[]> dataContact = new ArrayList<>();
    private ListContactAdapter listContactAdapter;
    private SessionManager session;
    private HashMap<String, String> userDetail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_save, container, false);

        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();

        edtCari = (EditText) view.findViewById(R.id.edtCari);
        layoutError = (RelativeLayout) view.findViewById(R.id.layoutError);
        listContact = (ListView) view.findViewById(R.id.listSaveContact);


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        modelContactSave = new ModelContactSave(getContext());
        edtCari.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();
            private final long DELAY = 1000; // Milliseconds

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                listContactAdapter.filter(edtCari.getText().toString().trim());
                listContact.invalidate();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        listContact.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listContact == null || listContact.getChildCount() == 0) ?
                                0 : listContact.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
        listContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String customerId = dataContact.get(i)[1];
                String customerName = dataContact.get(i)[2];
                String greeting = dataContact.get(i)[3];
                startActivity(new Intent(getContext(), DetailContactActivity.class)
                        .putExtra("CustomerId",customerId)
                        .putExtra("Greeting",greeting)
                        .putExtra("Name",customerName)
                );
            }
        });
        loadData();
        return view;
    }
    private void notfound(boolean isNotFound){
        if (isNotFound){
            layoutError.setVisibility(View.VISIBLE);
            listContact.setVisibility(View.GONE);
        }else{
            layoutError.setVisibility(View.GONE);
            listContact.setVisibility(View.VISIBLE);
        }
    }
    private void displayList() {
        listContactAdapter = new ListContactAdapter(dataContact,getContext());
        listContact.setAdapter(listContactAdapter);
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);
        dataContact.clear();
        List<String[]> list = modelContactSave.getAll(userDetail.get(KEY_USER_ID));
//        for (int i = 0;i<list.size();i++){
//            Log.e(TAG,"contactId:" + list.get(i)[0]);
//            Log.e(TAG,"userId:" + list.get(i)[7]);
//        }
        dataContact.addAll(list);
        displayList();
        swipeRefreshLayout.setRefreshing(false);
        if(list.size()>0){
            notfound(false);
        }else{
            notfound(true);
        }
    }


    class ListContactAdapter extends BaseAdapter {

        public List<String[]> listContact;

        public Context context;
        ArrayList<String[]> arraylist;

        private static final int resource = R.layout.item_contact;

        public ListContactAdapter(List<String[]> apps, Context context) {
            this.listContact = apps;
            this.context = context;
            arraylist = new ArrayList<String[]>();
            arraylist.addAll(listContact);

            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return listContact.size();
        }

        @Override
        public String[] getItem(int position) {
            return listContact.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = layoutInflater.inflate(resource,parent,false);

            TextView title = (TextView) view.findViewById(R.id.txtTitle);
            TextView subTitle = (TextView) view.findViewById(R.id.txtSubTitle);
            ImageView imgProfile = (ImageView) view.findViewById(R.id.imgProfile);
            ImageView whatsAppIcon = (ImageView) view.findViewById(R.id.whatsAppIcon);
            ImageView facebookIcon = (ImageView) view.findViewById(R.id.facebookIcon);
            ImageView instagramIcon = (ImageView) view.findViewById(R.id.instagramIcon);
            ImageView websiteIcon = (ImageView) view.findViewById(R.id.websiteIcon);
            ImageView tokopediaIcon = (ImageView) view.findViewById(R.id.tokopediaIcon);
            ImageView bukalapakIcon = (ImageView) view.findViewById(R.id.bukalapakIcon);
            ImageView shopeeIcon = (ImageView) view.findViewById(R.id.shopeeIcon);


            final String[] itemContact = getItem(position);
            String greeting = itemContact[3];
            String name = itemContact[2];
            String whatsApp = itemContact[4];
            String faceboook = itemContact[8];
            String instagram = itemContact[9];
            String website = itemContact[10];
            String tokopedia = itemContact[11];
            String bukalapak = itemContact[12];
            String shopee = itemContact[13];
            String foto = itemContact[16];
            Log.e(TAG,"foto:" + foto);
            String cityName = itemContact[15];

            title.setText(greeting +  " " + name);
            subTitle.setText(cityName); // kota

            if (whatsApp.equals("null") || whatsApp == null || whatsApp.equals(null) || whatsApp.equals("")){
                whatsAppIcon.setVisibility(View.GONE);
            }else{
                whatsAppIcon.setVisibility(View.VISIBLE);
                whatsAppIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String phoneNumber = whatsApp;
                        String text = "";// Replace with your message.
                        String toNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
                        if (TextUtils.isEmpty(text)){
                            text = "";
                        }
                        if (toNumber.substring(0,2).equals("62") == false){
                            toNumber = toNumber.replace("+","")
                                    .replace(" ","")
                                    .replace("-","")
                                    .replace("(","")
                                    .replace(")","");
                            if (toNumber.substring(0,2).equals("08")){
                                toNumber = "62" + toNumber.substring(1);
                            }
                            if (toNumber.substring(0).equals("8")){
                                toNumber = "62" + toNumber;
                            }
                        }
                        try {
                            if (TextUtils.isEmpty(phoneNumber)){
                                Toast.makeText(getContext(), "No. WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show();
                            }else{
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setPackage("com.whatsapp");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+ URLEncoder.encode(text, "UTF-8") ));
                                startActivity(intent);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (!(foto.equals("null") || foto == null || foto.equals(null) || foto.equals(""))){
                imgProfile.setClipToOutline(true);
                String urlFoto = API.BASE_URL_UPLOADS + foto;
                Picasso.with(getActivity()).load(urlFoto)
                        .error(R.drawable.blank_profile)
                        .placeholder(R.drawable.blank_profile)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .fit()
                        .into(imgProfile);
            }
            if (faceboook.equals("null") || faceboook == null || faceboook.equals(null) || faceboook.equals("")){
                facebookIcon.setVisibility(View.GONE);
            }else{
                facebookIcon.setVisibility(View.VISIBLE);
                facebookIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = "Facebook - " + greeting + " " + name;
                        String url = null;
                        if (faceboook.contains("facebook.com")){
                            url = faceboook;
                        }else if (faceboook.contains("http")){
                            url = faceboook;
                        }else{
                            url = Uri.parse("https://facebook.com/" +faceboook).toString();
                        }
                        if (!url.equals(null)){
                            startActivity(new Intent(getActivity(), WebviewActivity.class)
                                    .putExtra("title", title)
                                    .putExtra("url", url)
                            );
                        }
                    }
                });
            }

            if (instagram.equals("null") || instagram == null || instagram.equals(null) || instagram.equals("")){
                instagramIcon.setVisibility(View.GONE);
            }else{
                instagramIcon.setVisibility(View.VISIBLE);
                instagramIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = "Instagram - " + greeting + " " + name;
                        String url = null;
                        if (instagram.contains("instagram.com")){
                            url = instagram;
                        }else if (instagram.contains("http")){
                            url = instagram;
                        }else{
                            url = Uri.parse("https://instagram.com/" +instagram).toString();
                        }
                        if (!url.equals(null)){
                            startActivity(new Intent(getActivity(), WebviewActivity.class)
                                    .putExtra("title", title)
                                    .putExtra("url", url)
                            );
                        }
                    }
                });
            }

            if (website.equals("null") || website == null || website.equals(null) || website.equals("")){
                websiteIcon.setVisibility(View.GONE);
            }else{
                websiteIcon.setVisibility(View.VISIBLE);
                websiteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = "Website - " + greeting + " " + name;
                        String url = Uri.parse(website).toString();
                        if (!url.equals(null)){
                            startActivity(new Intent(getActivity(), WebviewActivity.class)
                                    .putExtra("title", title)
                                    .putExtra("url", url)
                            );
                        }
                    }
                });
            }

            if (tokopedia.equals("null") || tokopedia == null || tokopedia.equals(null) || tokopedia.equals("")){
                tokopediaIcon.setVisibility(View.GONE);
            }else{
                tokopediaIcon.setVisibility(View.VISIBLE);
                tokopediaIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = "Tokopedia - " + greeting + " " + name;
                        String url = null;
                        if (tokopedia.contains("tokopedia.com")){
                            url = tokopedia;
                        }else if (tokopedia.contains("http")){
                            url = tokopedia;
                        }else{
                            url = Uri.parse("https://tokopedia.com/" +tokopedia).toString();
                        }
                        if (!url.equals(null)){
                            startActivity(new Intent(getActivity(), WebviewActivity.class)
                                    .putExtra("title", title)
                                    .putExtra("url", url)
                            );
                        }
                    }
                });
            }
            if (bukalapak.equals("null") || bukalapak == null || bukalapak.equals(null) || bukalapak.equals("")){
                bukalapakIcon.setVisibility(View.GONE);
            }else{
                bukalapakIcon.setVisibility(View.VISIBLE);
                bukalapakIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = "Bukalapak - " + greeting + " " + name;
                        String url = null;
                        if (bukalapak.contains("bukalapak.com")){
                            url = bukalapak;
                        }else if (bukalapak.contains("http")){
                            url = bukalapak;
                        }else{
                            url = Uri.parse("https://bukalapak.com/" +bukalapak).toString();
                        }
                        if (!url.equals(null)){
                            startActivity(new Intent(getActivity(), WebviewActivity.class)
                                    .putExtra("title", title)
                                    .putExtra("url", url)
                            );
                        }
                    }
                });
            }
            if (shopee.equals("null") || shopee == null || shopee.equals(null) || shopee.equals("")){
                shopeeIcon.setVisibility(View.GONE);
            }else{
                shopeeIcon.setVisibility(View.VISIBLE);
                shopeeIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = "Shopee - " + greeting + " " + name;
                        String url = null;
                        if (shopee.contains("shopee.com")){
                            url = shopee;
                        }else if (shopee.contains("http")){
                            url = shopee;
                        }else{
                            url = Uri.parse("https://shopee.co.id/" +shopee).toString();
                        }
                        if (!url.equals(null)){
                            startActivity(new Intent(getActivity(), WebviewActivity.class)
                                    .putExtra("title", title)
                                    .putExtra("url", url)
                            );
                        }
                    }
                });
            }
            return view;
        }
        public void filter(String charText) {

            charText = charText.toLowerCase(Locale.getDefault());

            listContact.clear();
            if (charText.length() == 0) {
                listContact.addAll(arraylist);

            } else {
                for (String[] postDetail : arraylist) {
                    if (charText.length() != 0 && postDetail[3].toLowerCase(Locale.getDefault()).contains(charText)) {
                        listContact.add(postDetail);
                    }

                    else if (charText.length() != 0 && postDetail[2].toLowerCase(Locale.getDefault()).contains(charText)) {
                        listContact.add(postDetail);
                    }
                    else if (charText.length() != 0 && postDetail[4].toLowerCase(Locale.getDefault()).contains(charText)) {
                            listContact.add(postDetail);
                    }
                }
            }
            notifyDataSetChanged();
        }

    }

}
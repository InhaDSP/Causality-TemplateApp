package com.inha.dsp.asdryrunner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.inha.dsp.asdryrunner.driver.CausalityData;
import com.inha.dsp.asdryrunner.driver.Metadata;
import com.inha.dsp.causality.type.Action;
import com.inha.dsp.causality.type.Caption;
import com.inha.dsp.causality.type.Cause;
import com.inha.dsp.causality.type.Context;
import com.inha.dsp.causality.type.CustomAction;
import com.inha.dsp.causality.type.Media;
import com.inha.dsp.causality.type.Option;
import com.inha.dsp.causality.type.Perceptron;
import com.inha.dsp.causality.type.Role;
import com.inha.dsp.causality.type.Scenario;
import com.inha.dsp.causality.type.Scene;
import com.inha.dsp.causality.util.XmlLoader;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class BootstrapFragment extends Fragment {
    private EditText tbBootstrap = null;
    private ProgressBar pbBootstrap = null;
    private TextView tvLoading = null;
    private Button btLaunch = null;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bootstrap, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btLaunch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(BootstrapFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        tvLoading = view.findViewById(R.id.textview_loading);
        tbBootstrap = view.findViewById(R.id.tbBootstrap);
        pbBootstrap = view.findViewById(R.id.progressBarBootstrap);
        btLaunch = view.findViewById(R.id.btLaunch);

        pbBootstrap.setProgress(0);
        loadAllContent();
//        final ArrayList<CausalityData> datas = CausalityData.Datas;
//        CausalityData data = datas.get(0);
        pbBootstrap.setProgress(100);
        tvLoading.setText(getResources().getString(R.string.loading_finished));
        btLaunch.setEnabled(true);
    }

    private void loadAllContent()
    {
        String rootPath = "content/scenario";
        AssetManager assetManager = getResources().getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(rootPath);
            final int progressPerScenario = 100 / assets.length;
            for(String subPath : assets) {
                // subdirectory in content/scenario
                loadContentXml(rootPath + "/" + subPath + "/", progressPerScenario);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void loadContentXml(String rootPath, int baseProgress) {
        final int progressPerDocument = baseProgress / 12;
        XmlLoader loader = new XmlLoader();

        Document xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_role));
        ArrayList<Role> roles = loader.LoadRole(xmlDocument);
        for(Role role : roles) {
            String roleInfo = getString(R.string.format_role, role.getSerialNumber(),
                    role.getName(), role.getDescription(), role.getPosition());
            Log.d("BootstrapFragment", roleInfo);
        }
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_action));
        ArrayList<Action> actions = loader.LoadAction(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_perceptron));
        ArrayList<Perceptron> perceptrons = loader.LoadPerceptron(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_cause));
        ArrayList<Cause> causes = loader.LoadCause(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_option));
        ArrayList<Option> options = loader.LoadOption(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_caption));
        ArrayList<Caption> captions = loader.LoadCaption(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_scene));
        ArrayList<Scene> scenes = loader.LoadScene(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_scenario));
        ArrayList<Scenario> scenarios = loader.LoadScenario(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_context));
        ArrayList<Context> contexts = loader.LoadContext(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_media));
        ArrayList<Media> medias = loader.LoadMedia(xmlDocument);
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        Metadata metadata = LoadMetadata(rootPath + getResources().getString(R.string.filename_metadata));
        pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);

        try
        {
            xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_customaction));
            ArrayList<CustomAction> customactions = loader.LoadCustomAction(xmlDocument);

            CausalityData.CurrentData = new CausalityData(
                    actions, captions, causes, contexts, options,
                    perceptrons, roles, scenes, scenarios, customactions,
                    medias, metadata);
            pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);
        } catch (Exception e)
        {
            CausalityData.CurrentData = new CausalityData(
                    actions, captions, causes, contexts, options,
                    perceptrons, roles, scenes, scenarios, null,
                    medias, metadata);
            pbBootstrap.setProgress(pbBootstrap.getProgress() + progressPerDocument);
        }
    }

    private Document loadXmlFromAsset(String filePath)
    {
        Document result = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(filePath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringComments(true);
            dbFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder docBuild = dbFactory.newDocumentBuilder();
            result = docBuild.parse(is);
            result.getDocumentElement().normalize();
//            byte[] buffer = new byte[is.available()];
//            is.read(buffer);
//            result = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Metadata LoadMetadata(String filePath)
    {
        Node rootNode = loadXmlFromAsset(filePath).getLastChild();
        NodeList propertyNodes = rootNode.getChildNodes();
        Metadata metadata = new Metadata();
        for (int i = 0; i < propertyNodes.getLength(); i++)
        {
            Node propertyNode = propertyNodes.item(i);
            switch (propertyNode.getNodeName())
            {
                case "Name":
                    metadata.Name = propertyNode.getTextContent();
                    break;
                case "Layout":
                    metadata.Layout = propertyNode.getTextContent();
                    break;
            }
        }
        return metadata;
    }
}
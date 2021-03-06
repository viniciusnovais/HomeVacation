package pdasolucoes.com.br.homevacation;

import android.accounts.AuthenticatorException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pdasolucoes.com.br.homevacation.Adapter.ListaQuestaoAdapter;
import pdasolucoes.com.br.homevacation.Model.Ambiente;
import pdasolucoes.com.br.homevacation.Model.Questao;
import pdasolucoes.com.br.homevacation.Service.QuestaoService;

/**
 * Created by PDA on 11/10/2017.
 */

public class CadastroQuestaoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private ListaQuestaoAdapter adapter;
    private List<Questao> listaQuestao;
    private Ambiente ambiente;
    private TextView tvTituloBar;
    private ProgressDialog progressDialog;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        preferences = getSharedPreferences("Login", MODE_PRIVATE);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        tvTituloBar = (TextView) findViewById(R.id.tvtTituloToolbar);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        ambiente = (Ambiente) getIntent().getSerializableExtra("ambiente");

        tvTituloBar.setText(ambiente.getDescricao());

        DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncGetQuestao asyncGetQuestao = new AsyncGetQuestao();
                asyncGetQuestao.execute();
            }
        });
    }

    private class AsyncGetQuestao extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {

            List<Questao> lista = QuestaoService.GetListaQuestao();

            return lista;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            popupInsereQuestao((List<Questao>) o);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        AsyncQuestao task = new AsyncQuestao();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ambiente.getId());
    }

    public class AsyncQuestao extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CadastroQuestaoActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.load));
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<Questao> doInBackground(Object[] params) {

            listaQuestao = QuestaoService.GetListaQuestao((Integer) params[0]);

            return listaQuestao;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            adapter = new ListaQuestaoAdapter(CadastroQuestaoActivity.this, (List<Questao>) o);
            recyclerView.setAdapter(adapter);
        }
    }

    public class AsyncInsereQuestao extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] params) {

            Questao q = QuestaoService.SetQuestao((Questao) params[0]);
            return q;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            listaQuestao.add((Questao) o);
            adapter = new ListaQuestaoAdapter(CadastroQuestaoActivity.this, listaQuestao);
            recyclerView.setAdapter(adapter);
        }
    }

    private void popupInsereQuestao(final List<Questao> listaQuestao) {
        View v = View.inflate(CadastroQuestaoActivity.this, R.layout.popup_insere_nova_questao, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(CadastroQuestaoActivity.this);
        final AlertDialog dialog;
        RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radioGroupEvidence);
//        final TextInputEditText editQuestion = (TextInputEditText) v.findViewById(R.id.editQuestion);
        final Spinner spinnerQuestao = (Spinner) v.findViewById(R.id.spinner);
        Button btDone = (Button) v.findViewById(R.id.btDone);
        Button btCancel = (Button) v.findViewById(R.id.btCancel);
        final Questao questao = new Questao();

        builder.setView(v);
        dialog = builder.create();
        dialog.show();

        ArrayAdapter<Questao> arrayAdapter =
                new ArrayAdapter<>(CadastroQuestaoActivity.this, android.R.layout.simple_list_item_1, listaQuestao);
        spinnerQuestao.setAdapter(arrayAdapter);
        spinnerQuestao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Questao q = (Questao) parent.getItemAtPosition(position);
                questao.setDescricao(q.getDescricao());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                questao.setIdAmbiente(ambiente.getId());
                questao.setIdUsuario(preferences.getInt("idUsuario", 0));


                AsyncInsereQuestao task = new AsyncInsereQuestao();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, questao);

                dialog.dismiss();

            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                RadioButton r = (RadioButton) group.findViewById(checkedId);
                String evidence = "";

                if (r.getText().toString().equals("No")) {
                    evidence = "N";
                } else {
                    evidence = "S";
                }

                questao.setEvidencia(evidence);
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }
}

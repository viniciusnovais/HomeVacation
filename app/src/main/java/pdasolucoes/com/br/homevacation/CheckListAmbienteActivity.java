package pdasolucoes.com.br.homevacation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pdasolucoes.com.br.homevacation.Adapter.ImagesAmbienteAdapter;
import pdasolucoes.com.br.homevacation.Adapter.ListaChecklistAmbienteAdapter;
import pdasolucoes.com.br.homevacation.Dao.CheckListVoltaDao;
import pdasolucoes.com.br.homevacation.Dao.ChecklistDao;
import pdasolucoes.com.br.homevacation.Dao.FotosAmbienteDao;
import pdasolucoes.com.br.homevacation.Dao.PendenteDao;
import pdasolucoes.com.br.homevacation.Dao.QuestaoDao;
import pdasolucoes.com.br.homevacation.Dao.QuestaoVoltaDao;
import pdasolucoes.com.br.homevacation.Model.Ambiente;
import pdasolucoes.com.br.homevacation.Model.Casa;
import pdasolucoes.com.br.homevacation.Model.CheckList;
import pdasolucoes.com.br.homevacation.Model.CheckListVolta;
import pdasolucoes.com.br.homevacation.Model.FotoAmbiente;
import pdasolucoes.com.br.homevacation.Model.Questao;
import pdasolucoes.com.br.homevacation.Model.QuestaoCheckList;
import pdasolucoes.com.br.homevacation.Service.AmbienteService;
import pdasolucoes.com.br.homevacation.Service.CheckListService;
import pdasolucoes.com.br.homevacation.Service.FotoAmbienteService;
import pdasolucoes.com.br.homevacation.Util.ListaAmbienteDao;

/**
 * Created by PDA on 12/10/2017.
 */

public class CheckListAmbienteActivity extends AppCompatActivity {

    private TextView tvTitulo;
    List<Ambiente> listaAmbiente;
    private ListaChecklistAmbienteAdapter adapter;
    private ChecklistDao checklistDao;
    private QuestaoDao questaoDao;
    private CheckListVoltaDao checkListVoltaDao;
    private QuestaoVoltaDao questaoVoltaDao;
    private RecyclerView recyclerView;
    public static Activity AmbienteActivity;
    private ProgressDialog progressDialog, progressDialog2;
    private FloatingActionButton fab;
    private TextView tvTexto;
    private long lastBackPressTime = 0;
    private FotoAmbiente fotoAmbiente;
    private FotosAmbienteDao fotosAmbienteDao;
    private Intent i;
    private PendenteDao pendenteDao;


    //Nessa activity eu tenho q ter a foto da casa

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        AmbienteActivity = this;

        checklistDao = new ChecklistDao(this);
        questaoDao = new QuestaoDao(this);
        questaoVoltaDao = new QuestaoVoltaDao(this);
        checkListVoltaDao = new CheckListVoltaDao(this);
        pendenteDao = new PendenteDao(this);
        tvTitulo = (TextView) findViewById(R.id.tvtTituloToolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        tvTexto = (TextView) findViewById(R.id.tvTexto);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        AsyncAmbiente task = new AsyncAmbiente();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    //async fotos AMBIENTE
    private class AsyncFotos extends AsyncTask {

        ProgressDialog progressDialog;
        List<FotoAmbiente> lista;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CheckListAmbienteActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.load));
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            //Adicionar o serviço que retorna a lista de fotos
            lista = FotoAmbienteService.listar((Integer) params[0]);
            return lista;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                i.putExtra("fotoAmbiente", (Serializable) lista);
                startActivity(i);

            }

        }
    }

    public class AsyncAmbiente extends AsyncTask {
        SharedPreferences sharedPreferences = getSharedPreferences("listaAmbiente", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        List<QuestaoCheckList> listaQuestao;
        List<CheckList> lists;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CheckListAmbienteActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.load));
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            listaAmbiente = AmbienteService.getAmbiente(AgendaActivity.CASA);
            lists = CheckListService.GetListaCheckListItens(getIntent().getIntExtra("ID_CHECKLIST", 0));
            checklistDao.incluir(CheckListService.GetListaCheckListItens(getIntent().getIntExtra("ID_CHECKLIST", 0)));

            listaQuestao = CheckListService.GetCheckListQuestao(getIntent().getIntExtra("ID_CHECKLIST", 0));
            questaoDao.incluir(CheckListService.GetCheckListQuestao(getIntent().getIntExtra("ID_CHECKLIST", 0)));

            //faço esse if para quando finalizar o ambiente eu alterar a lista de ambiente para o proximo item ficar habilitado
            if (!getIntent().hasExtra("FINISH_ROOM")) {
                editor.putString("lista", ListaAmbienteDao.salvar(listaAmbiente)).commit();
            }
            return ListaAmbienteDao.listar(sharedPreferences.getString("lista", ""));
        }

        @Override
        protected void onPostExecute(final Object o) {
            super.onPostExecute(o);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();

                if (((List<Ambiente>) o).size() != 0 && (listaQuestao.size() != 0 || lists.size() != 0)) {


                    tvTitulo.setText(((List<Ambiente>) o).get(0).getDescricaoCasa());

                    adapter = new ListaChecklistAmbienteAdapter((List<Ambiente>) o, getApplicationContext());
                    recyclerView.setAdapter(adapter);

                    adapter.ItemClickListener(new ListaChecklistAmbienteAdapter.ItemClick() {
                        @Override
                        public void onClick(int position) {
                            if (((List<Ambiente>) o).get(position).isRespondido()) {
                                i = new Intent(CheckListAmbienteActivity.this, CheckListItemActivity.class);
                                i.putExtra("ambiente", (((List<Ambiente>) o).get(position)));
                                i.putExtra("ID_CHECKLIST", getIntent().getIntExtra("ID_CHECKLIST", 0));
                                //definir quais parametros vou passar para listar a foto do ambinete correto
                                AsyncFotos asyncFotos = new AsyncFotos();
                                asyncFotos.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((List<Ambiente>) o).get(position).getId());

                            } else {
                                Toast.makeText(CheckListAmbienteActivity.this, getString(R.string.previous_room), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    if (verificaAmbientes()) {
                        //lista de pendencias

                        if (!(getIntent().hasExtra("PENDENTE_OK"))) {
                            Intent i = new Intent(CheckListAmbienteActivity.this, PendenteActivity.class);
                            i.putExtra("ID_CHECKLIST", getIntent().getIntExtra("ID_CHECKLIST", 0));
                            i.putExtra("ID_CASA", getIntent().getIntExtra("ID_CASA", 0));
                            startActivity(i);
                        } else {
                            Toast.makeText(CheckListAmbienteActivity.this, getResources().getString(R.string.synchronize), Toast.LENGTH_SHORT).show();

                            final ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(fab,
                                    PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                                    PropertyValuesHolder.ofFloat("scaleY", 1.2f));
                            scaleDown.setDuration(300);

                            scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
                            scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

                            scaleDown.start();

                            fab.setVisibility(View.VISIBLE);
                            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(CheckListAmbienteActivity.this, R.color.colorPrimary)));
                            fab.setImageResource(R.drawable.ic_sync_black_24dp);
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    scaleDown.cancel();
                                    AsyncDevolverCheckList task = new AsyncDevolverCheckList();
                                    task.executeOnExecutor(THREAD_POOL_EXECUTOR, getIntent().getIntExtra("ID_CHECKLIST", 0));
                                }
                            });
                        }
                    }

                } else {
                    recyclerView.setVisibility(View.GONE);
                    tvTexto.setVisibility(View.VISIBLE);

                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            Toast.makeText(this, getResources().getString(R.string.back_press), Toast.LENGTH_SHORT).show();
            this.lastBackPressTime = System.currentTimeMillis();
        } else {
            if (verificaAmbientes()) {
                if (!(getIntent().hasExtra("PENDENTE_OK"))) {
                    Intent i = new Intent(CheckListAmbienteActivity.this, PendenteActivity.class);
                    i.putExtra("ID_CHECKLIST", getIntent().getIntExtra("ID_CHECKLIST", 0));
                    startActivity(i);
                } else {
                    popupFinishCheckList();
                }
            } else {
                Intent i = new Intent(CheckListAmbienteActivity.this, OpcaoEntradaActivity.class);
                startActivity(i);
                finish();
            }
        }
    }

    private boolean verificaAmbientes() {
        SharedPreferences sharedPreferences = getSharedPreferences("listaAmbiente", MODE_PRIVATE);
        for (Ambiente a : ListaAmbienteDao.listar(sharedPreferences.getString("lista", ""))) {
            if (!(checklistDao.qtdeItem(a.getId()) == 0 && questaoDao.qtdeQuestao(a.getId()) == 0)) {
                return false;
            }
        }
        return true;
    }

    private class AsyncDevolverCheckList extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog2 = new ProgressDialog(CheckListAmbienteActivity.this);
            progressDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog2.setMessage(getString(R.string.load));
            progressDialog2.setCanceledOnTouchOutside(true);
            progressDialog2.setCancelable(false);
            progressDialog2.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            boolean enviou = false;
            int resultQuestao = CheckListService.SetChecklistQuestao(questaoVoltaDao.listar((int) params[0]));
            int resultItem = CheckListService.SetChecklistItem(checkListVoltaDao.listar((int) params[0]));

            if (resultQuestao == 1 || resultItem == 1) {
                enviou = true;
            }

            return enviou;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (progressDialog2.isShowing()) {
                progressDialog2.dismiss();
                if ((boolean) o) {

                    checkListVoltaDao.export(checkListVoltaDao.listar(getIntent().getIntExtra("ID_CHECKLIST", 0)));
                    questaoVoltaDao.export(questaoVoltaDao.listar(getIntent().getIntExtra("ID_CHECKLIST", 0)));

                    checklistDao.deletar();
                    questaoDao.deletar();

                    //ao invés do Toast, será um popup
                    popupFinishCheckList();
                }
            }
        }
    }

    private class AsyncFinalizaCheck extends AsyncTask {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CheckListAmbienteActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            int result = CheckListService.FinalizaCheckList(Integer.parseInt(params[0].toString()));

            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }


    public void popupFinishCheckList() {
        View v = View.inflate(CheckListAmbienteActivity.this, R.layout.popup_msg, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(CheckListAmbienteActivity.this);
        Button btDone = (Button) v.findViewById(R.id.btDone);
        Button btCancel = (Button) v.findViewById(R.id.btCancel);
        btCancel.setVisibility(View.GONE);
        TextView tvConteudo = (TextView) v.findViewById(R.id.conteudo);
        TextView tvTitle = (TextView) v.findViewById(R.id.title);
        final AlertDialog dialog;
        builder.setView(v);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(false);

        tvTitle.setText(getString(R.string.congrants));

        tvConteudo.setText(getString(R.string.msg_congrants));
        btDone.setText(getString(R.string.ok));

        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent i = new Intent(CheckListAmbienteActivity.this, OpcaoEntradaActivity.class);

                AsyncFinalizaCheck task = new AsyncFinalizaCheck();
                task.execute(getIntent().getIntExtra("ID_CHECKLIST", 0));

                startActivity(i);
                finish();
            }
        });
        dialog.show();
    }

}

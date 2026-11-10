import sys, copy
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

AZUL='1F3864'; CLARO='DEEAF6'; GRIS='F2F2F2'
def styles(path):
    d=Document(path)
    st=d.styles
    for n in ['Normal','Body Text','First Paragraph','Compact']:
        try:
            s=st[n]; s.font.name='Calibri'; s.font.size=Pt(11)
            s.element.rPr.rFonts.set(qn('w:eastAsia'),'Calibri')
        except KeyError: pass
    for n,sz in [('Title',24),('Subtitle',16),('Heading 1',16),('Heading 2',14),('Heading 3',12),('Heading 4',11)]:
        s=next((x for x in st if x.name.lower()==n.lower() or x.style_id==n.replace(' ','')),None)
        if s is None: continue
        s.font.name='Calibri'; s.font.size=Pt(sz); s.font.bold=True; s.font.color.rgb=RGBColor.from_string(AZUL)
    d.save(path)

def shade(cell,color):
    tcPr=cell._tc.get_or_add_tcPr()
    for e in tcPr.findall(qn('w:shd')): tcPr.remove(e)
    sh=OxmlElement('w:shd'); sh.set(qn('w:val'),'clear'); sh.set(qn('w:color'),'auto'); sh.set(qn('w:fill'),color); tcPr.append(sh)

def borders(tbl):
    tblPr=tbl._tbl.tblPr
    for e in tblPr.findall(qn('w:tblBorders')): tblPr.remove(e)
    b=OxmlElement('w:tblBorders')
    for side in ['top','left','bottom','right','insideH','insideV']:
        x=OxmlElement('w:'+side); x.set(qn('w:val'),'single'); x.set(qn('w:sz'),'6'); x.set(qn('w:space'),'0'); x.set(qn('w:color'),'8EAADB'); b.append(x)
    tblPr.append(b)
    for e in tblPr.findall(qn('w:tblW')): tblPr.remove(e)
    w=OxmlElement('w:tblW'); w.set(qn('w:w'),'5000'); w.set(qn('w:type'),'pct'); tblPr.append(w)
    for e in tblPr.findall(qn('w:tblLayout')): tblPr.remove(e)
    lay=OxmlElement('w:tblLayout'); lay.set(qn('w:type'),'autofit'); tblPr.append(lay)
    mar=OxmlElement('w:tblCellMar')
    for side,v in [('top',40),('bottom',40),('left',80),('right',80)]:
        x=OxmlElement('w:'+side); x.set(qn('w:w'),str(v)); x.set(qn('w:type'),'dxa'); mar.append(x)
    tblPr.append(mar)

def repeat_header(row):
    trPr=row._tr.get_or_add_trPr()
    h=OxmlElement('w:tblHeader'); h.set(qn('w:val'),'true'); trPr.append(h)
    c=OxmlElement('w:cantSplit'); trPr.append(c)

def font_cells(tbl,size,header):
    for ri,row in enumerate(tbl.rows):
        for cell in row.cells:
            for p in cell.paragraphs:
                p.paragraph_format.space_before=Pt(1); p.paragraph_format.space_after=Pt(1)
                for r in p.runs:
                    r.font.size=Pt(size)
                    if ri==0 and header:
                        r.font.bold=True; r.font.color.rgb=RGBColor(255,255,255)

def colwidths(tbl, fracs, total_cm=16.5):
    grid=tbl._tbl.tblGrid
    for gc,f in zip(grid.findall(qn('w:gridCol')),fracs):
        gc.set(qn('w:w'),str(int(total_cm*f*567)))
    for row in tbl.rows:
        for cell,f in zip(row.cells,fracs):
            cell.width=Cm(total_cm*f)

import re
STEP=re.compile(r' (?=\d{1,2}[a-z]?\. [A-ZÁÉÍÓÚ\"])')
def split_steps(cell):
    p=cell.paragraphs[0]
    runs=list(p.runs)
    if not STEP.search(p.text): return
    segs=[]  # list of paragraphs, each list of (run_xml_copy,text)
    cur=[]
    for r in runs:
        parts=STEP.split(r.text)
        for k,t in enumerate(parts):
            if k>0: segs.append(cur); cur=[]
            if t: cur.append((r,t))
    segs.append(cur)
    base=p
    for k,seg in enumerate(segs):
        np_=base if k==0 else cell.add_paragraph()
        if k==0:
            for r in list(np_.runs): r._r.getparent().remove(r._r)
        for r,t in seg:
            nr=copy.deepcopy(r._r); np_._p.append(nr)
            from docx.text.run import Run
            Run(nr,np_).text=t
def main(path):
    d=Document(path)
    sec=d.sections[0]
    sec.page_width=Cm(21); sec.page_height=Cm(29.7)
    for m in ['left_margin','right_margin']: setattr(sec,m,Cm(2.2))
    sec.top_margin=Cm(2.2); sec.bottom_margin=Cm(2.2)
    for tbl in d.tables:
        tbl.alignment=WD_TABLE_ALIGNMENT.CENTER
        borders(tbl)
        n=len(tbl.columns)
        first=tbl.rows[0].cells[0].text.strip()
        ficha = n==2 and (first.startswith('RF-') or first=='Elemento' or first=='Datos del documento' or first=='Atributo')
        for c in tbl.rows[0].cells: shade(c,AZUL)
        repeat_header(tbl.rows[0])
        if n==2:
            for row in tbl.rows[1:]:
                shade(row.cells[0],CLARO)
                for p in row.cells[0].paragraphs:
                    for r in p.runs: r.font.bold=True
            colwidths(tbl,[0.26,0.74])
        else:
            for k,row in enumerate(tbl.rows[1:]):
                if k%2==1:
                    for c in row.cells: shade(c,GRIS)
        hdr=[c.text.strip() for c in tbl.rows[0].cells]
        W={('ID','Característica (ISO 25010)'):[.09,.14,.19,.25,.23,.1],
           ('ID','Nombre','Descripción / regla'):[.11,.15,.32,.32,.1],
           ('Requerimiento','Caso de uso'):[.18,.12,.22,.48],
           ('Condición','Ejemplo'):[.32,.3,.38],
           ('ID','Nombre','Tipo'):[.15,.5,.2,.15],
           ('Campo','Condición'):[.13,.3,.57]}
        for k,v in W.items():
            if tuple(hdr[:len(k)])==k and len(v)==n: colwidths(tbl,v)
        if hdr[:2]==['ID','Nombre'] and n==3: colwidths(tbl,[.2,.6,.2])
        if hdr[:2]==['ID','Requerimiento'] and n==3: colwidths(tbl,[.14,.6,.26])
        if n==2:
            for row in tbl.rows[1:]:
                if row.cells[0].text.strip() in ('Flujo básico','Flujos alternos','Proceso'):
                    split_steps(row.cells[1])
        font_cells(tbl, 8.5 if n>=5 else 9.5, True)
        for row in tbl.rows:
            c0=row.cells[0]
            for pp in c0.paragraphs:
                for r in pp.runs:
                    if r.text.startswith(('RF-','RNF-','CU-')): r.text=r.text.replace('-','\u2011')
        if len(tbl.rows)<=14:
            for row in tbl.rows[:-1]:
                for c in row.cells:
                    for pp in c.paragraphs: pp.paragraph_format.keep_with_next=True
    # images: center + caption style
    for i,p in enumerate(d.paragraphs):
        if p._p.xpath('.//pic:pic'):
            p.alignment=WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.keep_with_next=True
    for p in d.paragraphs:
        if p.style.name in ('Image Caption','Captioned Figure'):
            p.alignment=WD_ALIGN_PARAGRAPH.CENTER
            for r in p.runs: r.font.size=Pt(9.5); r.font.italic=True; r.font.color.rgb=RGBColor.from_string(AZUL)
    # page numbers footer
    fp=sec.footer.paragraphs[0]; fp.alignment=WD_ALIGN_PARAGRAPH.CENTER
    r=fp.add_run('Sistema de Gestión de Biblioteca · Requerimientos y Casos de Uso · Página '); r.font.size=Pt(8)
    for t,txt in [('begin',None),(None,'PAGE'),('end',None)]:
        run=fp.add_run(); run.font.size=Pt(8)
        if t:
            f=OxmlElement('w:fldChar'); f.set(qn('w:fldCharType'),t); run._r.append(f)
        else:
            it=OxmlElement('w:instrText'); it.set(qn('xml:space'),'preserve'); it.text=txt; run._r.append(it)
    d.save(path)

if sys.argv[1]=='styles': styles(sys.argv[2])
else: main(sys.argv[2])

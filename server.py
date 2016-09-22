from flask import Flask, request, session, g, Response
from flask.json import jsonify
import sys
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
import random
import ipdb
import cStringIO
from functools import wraps

# create our little application :)
app = Flask(__name__)
app.config.update(dict(
    DATABASE='postgres',
    HOST='localhost',
    DEBUG=True,
    SECRET_KEY='bar007',
    USERNAME='postgres',
))
app.config.from_object(__name__)

@app.route('/')
def index():
    return Response("This is Meso cluster master!!!!", 200)
    
def connect_db():
    try:
        conn = psycopg2.connect(
            "dbname='postgres' user='postgres'")
        #"host='localhost'")
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    except:
        print "Unable to connect to PG database"
    return conn
        

def get_db():
    """
    Opens a new database connection
    if there is none yet for the
    current application context.
    """
    if not hasattr(g, 'postgres_db'):
        g.postgres_db = connect_db()

    return g.postgres_db


def query_db(db, query, args=(), one=False):
    cur = db.cursor()
    cur.execute(query, args)
    r = [dict((cur.description[i][0], value)
              for i, value in enumerate(row)) for row in cur.fetchall()]
    return (r[0] if r else None) if one else r
    
def status_400_on_exception(f):
    """Decorator for generating a 400 bad request response."""
    @wraps(f)
    def _f(*args, **kwargs):
        try:
            retval = f(*args, **kwargs)
        except Exception as e:
            if app.debug:
                with closing(StringIO()) as s:
                    traceback.print_exception(*sys.exc_info(), file=s)
                    response = s.getvalue()
            else:
                response = repr(e)
            resp = jsonify(response=response, status={'success': False})
            resp.status_code = 400
            return resp
        else:
            return retval
    return _f


@app.route('/mesosmaster', methods=['POST'])
#@status_400_on_exception
def register_mesos_master():
    ipdb.set_trace()

    jd = request.get_json()
    if 'mesos_master_ip' not in jd or 'mesos_master_id' not in jd:
        return Response("Mesos_master_id is required",400)
    master_ip = jd['mesos_master_ip']
    master_id = jd['mesos_master_id']

    try:
        db = get_db()
        db.cursor().execute("INSERT INTO register_master_table VALUES "
                            "(%s, %s)", (master_ip, master_id))
        
    except psycopg2.IntegrityError as err:
        return Response("Mesos master already registered with id:{}".
                        format(master_id), 400)
    except Exception as err:
        #ipdb.set_trace()
        return Response("{}".format(err),400) 
        
    return Response("Successfully registered.\n The master ip:{}\n The master id:{}\n"
                    "Database updated".
                    format(master_ip, master_id), 200)


@app.route('/slave', methods=['POST'])
#@status_400_on_exception                                                       
def register_mesos_slave():
    #ipdb.set_trace()                                                            
    jd = request.get_json()
    if 'mesos_slave_ip' not in jd or 'mesos_slave_id' not in jd:
        return Response("mesos_slave_ip and mesos_slave_id are required",400)
    slave_ip = jd['mesos_slave_ip']
    slave_id = jd['mesos_slave_id']
    master_ip = jd['mesos_master_ip']
    master_id = jd['mesos_master_id']
    try:
#        ipdb.set_trace()
        db = get_db()
        db_cur = db.cursor()
        if db_cur.execute("SELECT EXISTS (SELECT * FROM register_master_table WHERE master_id = %s)" , ('master_id',)) :
            db_cur.execute("INSERT INTO register_slave_table VALUES"
                           "(%s,%s,%s)", (slave_ip, slave_id, master_id))
        else :
            db_cur.execute("INSERT INTO register_master_table VALUES"
                           "(%s,%s)" , (master_ip, master_id))
            db_cur.execute("INSERT INTO register_slave_table VALUES"
                           "(%s,%s,%s)", (slave_ip, slave_id, master_id))
    except psycopg2.IntegrityError as err:
        return Response("Mesos slave Already registered with ip:{} id:{}".
                        format(slave_ip, slave_id), 200)
    except Exception as err:
        return Response("{}".format(err),400)
    return Response("Successfully registered the slave with ip:{}\n"
                    "Database updated".
                    format(slave_ip), 200)


@app.route('/dockercontainer', methods=['POST'])
#@status_400_on_exception
def docker_container_register():
    #ipdb.set_trace()
    jd = request.get_json()
    
    if 'container_id' not in jd and 'container_status' not in jd:
        return Response("The docker container id and the status is required", 400)
    slave_ip = jd['mesos_slave_ip']
    slave_id = jd['mesos_slave_id']
    master_ip = jd['mesos_master_ip']
    master_id = jd['mesos_master_id']    
    docker_container_id = jd['container_id']
    docker_container_status = jd['container_status']
    
    try:
        db = get_db()
        db_cur = db.cursor()
        if db_cur.execute("SELECT EXISTS (SELECT * FROM register_slave_table WHERE slave_id = %s)" ('slave_id',)):
            db_cur.execute("INSERT INTO docker_container_table VALUES" 
                           "(%s, %s, %s)", (docker_container_id, docker_container_status,
                                            slave_id))
        else: 
            db_cur.execute("INSERT INTO register_master_table VALUES"
                           "(%s, %s)" , (master_ip, master_id))
            db_cur.execute("INSERT INTO register_slave_table VALUES"
                           "(%s, %s, %s)", (slave_ip, slave_id, master_id))
            db_cur.execute("INSERT INTO docker_container_table VALUES"
                           "(%s, %s, %s)", (docker_container_id, 
                                            docker_contanier_status, slave_id))

    except psycopg2.IntegrityError as err:
        return Response("Docker already registered with id:{}\n".
                        format(docker_container_id), 200)
    except Exception as err:
            return Response("{}".format(err),400)
            
    return Response("The Docker details are now added to the database table\n" \
                    "The Docker_id is - {}.\n" \
                    "The status of this container is - {}.\n".
                    format (docker_container_id,
                            docker_container_status),200)
    

    
if __name__ == '__main__':
    if len(sys.argv) >= 2:
        port = int(sys.argv[1])
    else:
        port = 4000

    app.run(debug=True, host='0.0.0.0', port=port)
